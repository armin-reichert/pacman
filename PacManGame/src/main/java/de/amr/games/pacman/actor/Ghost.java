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
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.SteeringBehavior;
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
public class Ghost extends Actor<GhostState> implements GhostBehaviors {

	public final PacManGame game;
	public final String name;
	public Supplier<GhostState> fnNextState;
	public final Tile initialTile;
	public final int initialDir;
	public int foodCount;

	private Map<GhostState, SteeringBehavior> behaviorMap = new EnumMap<>(GhostState.class);

	public Ghost(PacManGame game, String name, GhostColor color, Tile initialTile, int initialDir) {
		this.game = game;
		this.maze = game.maze;
		this.name = name;
		this.initialTile = initialTile;
		this.initialDir = initialDir;
		fnNextState = this::getState;
		buildStateMachine();
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

	@Override
	public String name() {
		return name;
	}

	@Override
	public Ghost self() {
		return this;
	}

	private void chasingSoundOn() {
		if (!game.theme.snd_ghost_chase().isRunning()) {
			game.theme.snd_ghost_chase().loop();
		}
	}

	private void chasingSoundOff() {
		// if this is the only chasing ghost, turn it off
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
		// if this is the only dead ghost, turn it off
		if (game.activeGhosts().filter(ghost -> ghost != this).noneMatch(ghost -> ghost.getState() == DEAD)) {
			game.theme.snd_ghost_dead().stop();
		}
	}

	@Override
	public void init() {
		super.init();
		initialize();
	}

	private void initialize() {
		placeAtTile(initialTile, TS / 2, 0);
		moveDir = initialDir;
		nextDir = initialDir;
		visible = true;
		sprites.select("color-" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
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

	public void setBehavior(GhostState state, SteeringBehavior behavior) {
		behaviorMap.put(state, behavior);
	}

	public SteeringBehavior currentBehavior() {
		return behaviorMap.getOrDefault(getState(), keepingDirection());
	}

	@Override
	public void steer() {
		currentBehavior().steer(this);
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (maze.isDoor(tile)) {
			return getState() == DEAD || getState() != LOCKED && maze.inGhostHouse(currentTile());
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
					.onEntry(this::chasingSoundOn)
					.onTick(this::move)
					.onExit(this::chasingSoundOff)
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						currentBehavior().computePath(this); 
					})
					.onTick(() -> {
						move();
						sprites.select(maze.inGhostHouse(currentTile())	
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
					.condition(() -> currentTile().equals(maze.ghostRevival))
					.act(() -> moveDir = initialDir)
				
		.endStateMachine();
		/*@formatter:on*/
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}
}