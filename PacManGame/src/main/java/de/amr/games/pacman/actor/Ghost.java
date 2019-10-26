package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Behavior;
import de.amr.games.pacman.actor.behavior.GhostBehaviors;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * <p>
 * The behavior of a ghost is controlled by a finite state machine.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeMover implements GhostBehaviors {

	public final StateMachine<GhostState, PacManGameEvent> fsm;
	public Supplier<GhostState> fnNextState; // state after FRIGHTENED or LOCKED state
	private final Map<GhostState, Behavior<Ghost>> behaviorMap;
	public final String name;
	private final Tile initialTile;
	private final int initialDir;
	public int foodCount;

	public Ghost(PacManGame game, String name, GhostColor color, Tile initialTile, int initialDir) {
		super(game);
		this.name = name;
		this.initialTile = initialTile;
		this.initialDir = initialDir;
		setSprites(color);
		behaviorMap = new EnumMap<>(GhostState.class);
		fsm = buildStateMachine();
		fsm.setIgnoreUnknownEvents(true);
		fnNextState = this::getState; // default is to keep state
	}

	@Override
	public Ghost self() {
		return this;
	}

	private void setSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			sprites.set("s_color_" + dir, game.theme.spr_ghostColored(color, dir));
			sprites.set("s_eyes_" + dir, game.theme.spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			sprites.set("s_value" + i, game.theme.spr_greenNumber(i));
		}
		sprites.set("s_frightened", game.theme.spr_ghostFrightened());
		sprites.set("s_flashing", game.theme.spr_ghostFlashing());
	}

	private void sirenOn() {
		if (!game.theme.snd_ghost_chase().isRunning()) {
			game.theme.snd_ghost_chase().loop();
		}
	}

	private void sirenOff() {
		if (game.activeGhosts().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.getState() == CHASING)) {
			game.theme.snd_ghost_chase().stop();
		}
	}

	private void deadSoundOn() {
		if (!game.theme.snd_ghost_dead().isRunning()) {
			game.theme.snd_ghost_dead().loop();
		}
	}

	private void deadSoundOff() {
		if (game.activeGhosts().filter(ghost -> ghost != this).noneMatch(ghost -> ghost.getState() == DEAD)) {
			game.theme.snd_ghost_dead().stop();
		}
	}

	public void initialize() {
		placeAtTile(initialTile, TS / 2, 0);
		setMoveDir(initialDir);
		setNextDir(initialDir);
		sprites.select("s_color_" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
	}

	// Behavior

	@Override
	public float getSpeed() {
		return game.getGhostSpeed(this);
	}

	public GhostState getNextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
	}

	public void setBehavior(GhostState state, Behavior<Ghost> behavior) {
		behaviorMap.put(state, behavior);
	}

	public Behavior<Ghost> currentBehavior() {
		return behaviorMap.getOrDefault(getState(), keepingDirection());
	}

	@Override
	public OptionalInt supplyIntendedDir() {
		return currentBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (game.maze.isDoor(tile)) {
			return getState() == DEAD || getState() != LOCKED && game.maze.inGhostHouse(tile());
		}
		return super.canEnterTile(tile);
	}

	@Override
	protected void move() {
		super.move();
		sprites.select("s_color_" + getMoveDir());
	}

	// Define state machine

	private StateMachine<GhostState, PacManGameEvent> buildStateMachine() {
		return StateMachine
		/*@formatter:off*/
		.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.onTick(this::move)
					.onExit(game.pacMan::resetEatTimer)
				
				.state(SCATTERING)
					.onTick(this::move)
			
				.state(CHASING)
					.onEntry(this::sirenOn)
					.onTick(this::move)
					.onExit(this::sirenOff)
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						currentBehavior().computePath(this); 
					})
					.onTick(() -> {
						move();
						sprites.select(game.maze.inGhostHouse(tile())	
									? "s_color_" + getMoveDir()
									: game.pacMan.isPowerEnding()	? "s_flashing" : "s_frightened");
					})
				
				.state(DYING)
					.timeoutAfter(game::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("s_value" + game.getGhostsKilledByEnergizer()); 
						game.addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(this::deadSoundOn)
					.onTick(() -> {	
						move();
						sprites.select("s_eyes_" + getMoveDir());
					})
					.onExit(this::deadSoundOff)
					
			.transitions()

				.when(LOCKED).then(FRIGHTENED)
					.condition(() -> game.canLeaveGhostHouse(this) && game.pacMan.hasPower())

				.when(LOCKED).then(SCATTERING)
					.condition(() -> game.canLeaveGhostHouse(this) && getNextState() == SCATTERING)
				
				.when(LOCKED).then(CHASING)
					.condition(() -> game.canLeaveGhostHouse(this) && getNextState() == CHASING)
				
				.when(CHASING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(CHASING).then(DYING).on(GhostKilledEvent.class)
				.when(CHASING).then(SCATTERING).on(StartScatteringEvent.class)

				.when(SCATTERING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(SCATTERING).then(DYING).on(GhostKilledEvent.class)
				.when(SCATTERING).then(CHASING).on(StartChasingEvent.class)
				
				.when(FRIGHTENED).then(CHASING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == CHASING)

				.when(FRIGHTENED).then(SCATTERING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == SCATTERING)
				
				.when(FRIGHTENED).then(DYING).on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD).onTimeout()
					
				.when(DEAD).then(LOCKED)
					.condition(() -> tile().equals(game.maze.getGhostRevivalTile()))
				
		.endStateMachine();
		/*@formatter:on*/
	}

	// Integrate state machine

	@Override
	public void init() {
		initialize();
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	public GhostState getState() {
		return fsm.getState();
	}

	public State<GhostState, PacManGameEvent> getStateObject() {
		return fsm.state();
	}

	public void setState(GhostState state) {
		fsm.setState(state);
	}

	public void processEvent(PacManGameEvent event) {
		fsm.process(event);
	}
}