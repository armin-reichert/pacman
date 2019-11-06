package de.amr.games.pacman.actor;

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
import java.util.logging.Logger;

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
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeMoverUsingFSM<GhostState, PacManGameEvent>
		implements GhostBehaviors {

	public Supplier<GhostState> fnNextState; // state after FRIGHTENED or LOCKED state
	public int foodCount;
	private final Tile initialTile;
	private final int initialDir;
	private final Map<GhostState, Behavior> behaviorMap = new EnumMap<>(GhostState.class);

	public Ghost(PacManGame game, String name, GhostColor color, Tile initialTile, int initialDir) {
		super(game, name);
		this.initialTile = initialTile;
		this.initialDir = initialDir;
		setSprites(color);
		buildStateMachine();
		fnNextState = this::getState; // default: keep state
	}

	@Override
	public Ghost self() {
		return this;
	}

	private void setSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			sprites.set("color-" + dir, game.theme.spr_ghostColored(color, dir));
			sprites.set("eyes-" + dir, game.theme.spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			sprites.set("value-" + i, game.theme.spr_greenNumber(i));
		}
		sprites.set("frightened", game.theme.spr_ghostFrightened());
		sprites.set("flashing", game.theme.spr_ghostFlashing());
	}

	private void sirenOn() {
		if (!game.theme.snd_ghost_chase().isRunning()) {
			game.theme.snd_ghost_chase().loop();
		}
	}

	private void sirenOff() {
		if (game.activeGhosts().filter(ghost -> this != ghost)
				.noneMatch(ghost -> ghost.getState() == CHASING)) {
			game.theme.snd_ghost_chase().stop();
		}
	}

	private void deadSoundOn() {
		if (!game.theme.snd_ghost_dead().isRunning()) {
			game.theme.snd_ghost_dead().loop();
		}
	}

	private void deadSoundOff() {
		if (game.activeGhosts().filter(ghost -> ghost != this)
				.noneMatch(ghost -> ghost.getState() == DEAD)) {
			game.theme.snd_ghost_dead().stop();
		}
	}

	@Override
	public void init() {
		initialize();
		super.init();
	}

	public void initialize() {
		placeAtTile(initialTile, TS / 2, 0);
		moveDir = initialDir;
		nextDir = initialDir;
		sprites.select("color-" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
		setVisible(true);
	}

	// Behavior

	@Override
	public float maxSpeed() {
		return game.computeGhostSpeed(this);
	}

	public GhostState getNextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
	}

	public void setBehavior(GhostState state, Behavior behavior) {
		behaviorMap.put(state, behavior);
	}

	public Behavior currentBehavior() {
		return behaviorMap.getOrDefault(getState(), keepingDirection());
	}

	@Override
	public OptionalInt getNextMoveDirection() {
		return currentBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (game.maze.isDoor(tile)) {
			return getState() == DEAD || getState() != LOCKED && game.maze.inGhostHouse(currentTile());
		}
		return super.canEnterTile(tile);
	}

	@Override
	protected void move() {
		super.move();
		sprites.select("color-" + moveDir);
	}

	// Define state machine

	private void buildStateMachine() {
		fsm = StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.onTick(this::move)
					.onExit(() -> game.pacMan.ticksSinceLastMeal = 0)
				
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
						sprites.select(game.maze.inGhostHouse(currentTile())	
									? "color-" + moveDir
									: game.pacMan.isLosingPower()	? "flashing" : "frightened");
					})
				
				.state(DYING)
					.timeoutAfter(game::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("value-" + game.numGhostsKilledByCurrentEnergizer()); 
						game.addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(this::deadSoundOn)
					.onTick(() -> {	
						move();
						sprites.select("eyes-" + moveDir);
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
					.condition(() -> currentTile().equals(game.maze.getGhostRevivalTile()))
				
		.endStateMachine();
		/*@formatter:on*/
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}
}