package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.speed;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.GhostSteerings;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameLevel;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Actor<GhostState> {

	private final Map<GhostState, Steering<Ghost>> steeringByState;
	private final Steering<Ghost> defaultSteering;

	public int initialDir;
	public Tile initialTile;
	public Tile revivalTile;
	public Tile scatterTile;
	public Supplier<GhostState> fnNextState;
	public Supplier<Tile> fnChasingTarget;
	public int foodCount;

	public Ghost(PacManGame game, Maze maze, String name) {
		super(name, game, maze);
		steeringByState = new EnumMap<>(GhostState.class);
		defaultSteering = GhostSteerings.headingForTargetTile();
		fnNextState = this::getState;
		fsm = buildStateMachine();
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	private void chasingSoundOn() {
		if (!theme.snd_ghost_chase().isRunning()) {
			theme.snd_ghost_chase().loop();
		}
	}

	private void chasingSoundOff() {
		// if this is the only chasing ghost, turn it off
		if (game.activeGhosts().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.getState() == CHASING)) {
			theme.snd_ghost_chase().stop();
		}
	}

	private void deadSoundOn() {
		if (!theme.snd_ghost_dead().isRunning()) {
			theme.snd_ghost_dead().loop();
		}
	}

	private void deadSoundOff() {
		// if this is the only dead ghost, turn it off
		if (game.activeGhosts().filter(ghost -> ghost != this).noneMatch(ghost -> ghost.getState() == DEAD)) {
			theme.snd_ghost_dead().stop();
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

	public void setSteering(GhostState state, Steering<Ghost> steering) {
		steeringByState.put(state, steering);
	}

	public Steering<Ghost> getSteering() {
		return steeringByState.getOrDefault(getState(), defaultSteering);
	}

	@Override
	public void steer() {
		getSteering().steer(this);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isDoor(neighbor)) {
			return getState() == ENTERING_HOUSE || getState() == LEAVING_HOUSE;
		}
		if (maze.isNoUpIntersection(tile) && neighbor == maze.tileToDir(tile, Top4.N)) {
			return getState() != CHASING && getState() != SCATTERING;
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	/* TODO: Some values are still guessed */
	public float maxSpeed() {
		PacManGameLevel level = game.level();
		boolean inTunnel = maze.isTunnel(tile());
		switch (getState()) {
		case LOCKED:
			//$FALL-THROUGH$
		case LEAVING_HOUSE:
			//$FALL-THROUGH$
		case ENTERING_HOUSE:
			return speed(level.ghostSpeed) / 2;
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return inTunnel ? speed(level.ghostTunnelSpeed) : speed(level.ghostSpeed);
		case FRIGHTENED:
			return inTunnel ? speed(level.ghostTunnelSpeed) : speed(level.ghostFrightenedSpeed);
		case DYING:
			return 0;
		case DEAD:
			return 2 * speed(level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), name));
		}
	}

	private void walkAndDisplayAs(String spriteKey) {
		steer();
		move();
		sprites.select(spriteKey);
	}

	private void turnBack() {
		nextDir = moveDir = NESW.inv(moveDir);
		enteredNewTile = true;
	}

	private boolean leftHouse() {
		Tile currentTile = tile();
		return !maze.partOfGhostHouse(currentTile) && tf.getY() - currentTile.row * TS == 0;
	}

	public static int getDyingTime() {
		return sec(1);
	}

	public GhostState nextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
	}

	private StateMachine<GhostState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir))
					.onExit(() -> {
						enteredNewTile = true;
						game.pacMan.ticksSinceLastMeal = 0;
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> targetTile = maze.blinkyHome)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir))
					.onExit(() -> moveDir = nextDir = Top4.W)
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> targetTile = revivalTile)
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))
				
				.state(SCATTERING)
					.onEntry(() -> targetTile = scatterTile)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir))
			
				.state(CHASING)
					.onEntry(() -> chasingSoundOn())
					.onTick(() -> {
						targetTile = fnChasingTarget.get();
						walkAndDisplayAs("color-" + moveDir);
					})
					.onExit(this::chasingSoundOff)
				
				.state(FRIGHTENED)
					.onTick(() -> walkAndDisplayAs(game.pacMan.isLosingPower() ? "flashing" : "frightened"))
				
				.state(DYING)
					.timeoutAfter(Ghost::getDyingTime)
					.onEntry(() -> {
						sprites.select("value-" + game.numGhostsKilledByEnergizer);
					})
				
				.state(DEAD)
					.onEntry(() -> {
						targetTile = maze.blinkyHome;
						deadSoundOn();
					})
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))
					.onExit(this::deadSoundOff)
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> leftHouse() && nextState() == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> leftHouse() && nextState() == CHASING)
					
				.when(ENTERING_HOUSE).then(LOCKED)
					.condition(() -> tile() == targetTile)
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnBack)
				
				.when(CHASING).then(DYING)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.on(StartScatteringEvent.class)
					.act(this::turnBack)

				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnBack)
				
				.when(SCATTERING).then(DYING)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.on(StartChasingEvent.class)
					.act(this::turnBack)
				
				.when(FRIGHTENED).then(CHASING)
					.on(PacManLostPowerEvent.class)
					.condition(() -> nextState() == CHASING)

				.when(FRIGHTENED).then(SCATTERING)
					.on(PacManLostPowerEvent.class)
					.condition(() -> nextState() == SCATTERING)
				
				.when(FRIGHTENED).then(DYING)
					.on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD)
					.onTimeout()
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> tile().equals(maze.blinkyHome))
				
		.endStateMachine();
		/*@formatter:on*/
	}
}