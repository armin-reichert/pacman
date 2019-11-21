package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.*;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static de.amr.games.pacman.model.PacManGame.relSpeed;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_FRIGHTENED_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_TUNNEL_SPEED;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.GhostBehaviors;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Actor<GhostState> implements GhostBehaviors {

	public final PacManGame game;

	public final String name;

	public final Tile initialTile;

	public final int initialDir;

	/** Function providing the next state after being FRIGHTENED or LOCKED. */
	public Supplier<GhostState> fnNextState;

	public int foodCount;

	private StateMachine<GhostState, PacManGameEvent> fsm;

	private Map<GhostState, Steering> steeringInState = new EnumMap<>(GhostState.class);

	public Ghost(PacManGame game, Maze maze, String name, GhostColor color, Tile initialTile, int initialDir) {
		super(maze);
		this.game = game;
		this.name = name;
		this.initialTile = initialTile;
		this.initialDir = initialDir;
		this.nextDir = initialDir;
		this.moveDir = initialDir;
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
	protected StateMachine<GhostState, PacManGameEvent> fsm() {
		return fsm;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Ghost theGhost() {
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
		placeAtTile(initialTile, TS / 2, 0);
		moveDir = initialDir;
		nextDir = initialDir;
		visible = true;
		sprites.select("color-" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
	}

	// Steering

	public void setBehavior(GhostState state, Steering steering) {
		steeringInState.put(state, steering);
	}

	@Override
	public void steer() {
		steeringInState.getOrDefault(getState(), keepingDirection()).steer(this);
	}

	@Override
	public boolean canEnterTile(Tile current, Tile tile) {
		if (maze.isDoor(tile)) {
			return getState() == RECOVERING || maze.inGhostHouse(current) && getState() != LOCKED;
		}
		if (maze.isNoUpIntersection(current) && tile == maze.tileToDir(current, Top4.N)) {
			return getState() != GhostState.CHASING && getState() != GhostState.SCATTERING;
		}
		return super.canEnterTile(current, tile);
	}

	@Override
	protected void move() {
		super.move();
		sprites.select("color-" + moveDir);
	}

	@Override
	/* TODO: some values still guessed */
	public float maxSpeed() {
		Tile tile = currentTile();
		int level = game.getLevel();
		if (maze.inGhostHouse(tile)) {
			return relSpeed(.25f);
		}
		float tunnelSpeed = relSpeed(GHOST_TUNNEL_SPEED.$float(level));
		switch (getState()) {
		case LOCKED:
			return 0; // locked outside ghost house
		case CHASING:
		case SCATTERING:
			return maze.isTunnel(tile) ? tunnelSpeed : relSpeed(GHOST_SPEED.$float(level));
		case FRIGHTENED:
			return maze.isTunnel(tile) ? tunnelSpeed : relSpeed(GHOST_FRIGHTENED_SPEED.$float(level));
		case DYING:
			return 0;
		case DEAD:
			return 2 * relSpeed(GHOST_SPEED.$float(level));
		case RECOVERING:
			return relSpeed(.25f);
		default:
			throw new IllegalStateException("Illegal ghost state for " + name);
		}
	}

	private void reverseDirection() {
		int oppositeDir = NESW.inv(moveDir);
		IntStream.of(oppositeDir, NESW.left(oppositeDir), NESW.right(oppositeDir)).filter(this::canEnterTileTo)
				.findFirst().ifPresent(dir -> nextDir = dir);
	}

	// Define state machine

	public static int getDyingTime() {
		return sec(1);
	}

	public GhostState getNextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
	}

	private void buildStateMachine() {
		fsm = StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.onTick(this::move)
					.onExit(() -> {
						game.pacMan.ticksSinceLastMeal = 0;
						enteredNewTile = true;
					})
				
				.state(SCATTERING)
					.onTick(this::move)
			
				.state(CHASING)
					.onEntry(this::chasingSoundOn)
					.onTick(this::move)
					.onExit(this::chasingSoundOff)
				
				.state(FRIGHTENED)
					.onEntry(this::reverseDirection)
					.onTick(() -> {
						move();
						sprites.select(maze.inGhostHouse(currentTile())	
									? "color-" + moveDir
									: game.pacMan.isLosingPower()	? "flashing" : "frightened");
					})
				
				.state(DYING)
					.timeoutAfter(Ghost::getDyingTime)
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
					
				.state(RECOVERING)
					.onEntry(() -> {
						targetTile = maze.ghostRevival;
					})
					.onTick(() -> {
						move();
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
				
				.when(CHASING).then(SCATTERING).on(StartScatteringEvent.class).act(this::reverseDirection)

				.when(SCATTERING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				
				.when(SCATTERING).then(DYING).on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING).on(StartChasingEvent.class).act(this::reverseDirection)
				
				.when(FRIGHTENED).then(CHASING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == CHASING)

				.when(FRIGHTENED).then(SCATTERING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == SCATTERING)
				
				.when(FRIGHTENED).then(DYING).on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD).onTimeout()
					
				.when(DEAD).then(RECOVERING)
					.condition(() -> currentTile().equals(maze.blinkyHome))
					.act(() -> targetTile = maze.pinkyHome)
					
				.when(RECOVERING).then(LOCKED)
					.condition(() -> currentTile().equals(maze.pinkyHome))
				
		.endStateMachine();
		/*@formatter:on*/
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}
}