package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
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
import de.amr.games.pacman.actor.behavior.GhostBehavior;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeMover implements GhostBehavior {

	private final PacManGame game;
	private final String name;
	private final StateMachine<GhostState, PacManGameEvent> fsm;
	private final Map<GhostState, Behavior<Ghost>> behaviorMap;
	private final Tile initialTile;
	private final Tile revivalTile;
	private final Tile scatteringTarget;
	private final int initialDir;
	private int foodCounter;

	public Supplier<GhostState> fnNextState; // chasing or scattering

	public Ghost(PacManGame game, String name, Tile initialTile, Tile scatteringTarget,
			int initialDir) {
		super(game.maze);
		this.game = game;
		this.name = name;
		this.initialTile = initialTile;
		this.revivalTile = maze.getPinkyHome();
		this.scatteringTarget = scatteringTarget;
		this.initialDir = initialDir;
		behaviorMap = new EnumMap<>(GhostState.class);
		fsm = buildStateMachine();
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(LOGGER, app().clock::getFrequency);
		fnNextState = this::getState; // default
	}

	public void setSprites(GhostColor color) {
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

	@Override
	public Ghost self() {
		return this;
	}

	public void initGhost() {
		placeAtTile(initialTile, TS / 2, 0);
		setMoveDir(initialDir);
		setNextDir(initialDir);
		sprites.select("s_color_" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
	}

	public void resetFoodCounter() {
		foodCounter = 0;
	}

	public void incFoodCounter() {
		foodCounter++;
	}

	private void reviveGhost() {
		placeAtTile(revivalTile, TS / 2, 0);
		setMoveDir(Top4.N);
		setNextDir(Top4.N);
		sprites.select("s_color_" + getMoveDir());
		sprites.forEach(Sprite::resetAnimation);
	}

	// Accessors

	public String getName() {
		return name;
	}

	public Tile getInitialTile() {
		return initialTile;
	}

	public Tile getRevivalTile() {
		return revivalTile;
	}

	public Tile getScatteringTarget() {
		return scatteringTarget;
	}

	public GhostState getNextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
	}

	@Override
	public float getSpeed() {
		return game.getGhostSpeed(this);
	}

	public int getFoodCounter() {
		return foodCounter;
	}

	// Behavior

	public void setBehavior(GhostState state, Behavior<Ghost> behavior) {
		behaviorMap.put(state, behavior);
	}

	public Behavior<Ghost> getBehavior() {
		return behaviorMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public OptionalInt supplyIntendedDir() {
		return getBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (maze.isWall(tile)) {
			return false;
		}
		if (maze.isDoor(tile)) {
			return getState() == DEAD || getState() != LOCKED && maze.inGhostHouse(getTile());
		}
		return true;
	}

	// State machine

	@Override
	public void init() {
		initGhost();
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

	private StateMachine<GhostState, PacManGameEvent> buildStateMachine() {
		/*@formatter:off*/
		return StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getMoveDir());
					})
					.onExit(() -> {
						game.pacMan.resetEatTimer();
					})
				
				.state(SCATTERING)
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getMoveDir()); 
					})
			
				.state(CHASING)
					.onEntry(() -> game.theme.snd_ghost_chase().loop())
					.onExit(() -> game.theme.snd_ghost_chase().stop())
					.onTick(() -> {	
						move();	
						sprites.select("s_color_" + getMoveDir()); 
					})
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						getBehavior().computePath(this); 
					})
					.onTick(() -> {
						move();
						sprites.select(game.pacMan.isPowerEnding() ? "s_flashing" : "s_frightened");
					})
				
				.state(DYING)
					.timeoutAfter(game::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("s_value" + game.getGhostsKilledByEnergizer()); 
						game.addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(() -> {
						game.theme.snd_ghost_dead().loop();
					})
					.onTick(() -> {	
						move();
						sprites.select("s_eyes_" + getMoveDir());
					})
					.onExit(() -> {
						if (game.activeGhosts().filter(ghost -> ghost != this)
								.noneMatch(ghost -> ghost.getState() == DEAD)) {
							game.theme.snd_ghost_dead().stop();
						}
					})
					
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
					.condition(() -> getTile().equals(getRevivalTile()))
					.act(this::reviveGhost)
				
		.endStateMachine();
		/*@formatter:on*/
	}
}