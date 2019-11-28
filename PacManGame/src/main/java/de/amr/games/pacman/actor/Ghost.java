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
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_FRIGHTENED_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_TUNNEL_SPEED;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.GhostSteerings;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Actor<GhostState> {

	private StateMachine<GhostState, PacManGameEvent> fsm;
	private final Map<GhostState, Steering<Ghost>> steeringByState;
	private final Steering<Ghost> defaultSteering;
	public Tile initialTile;
	public int initialDir;
	public Tile revivalTile;
	public Tile scatterTile;
	public Supplier<GhostState> fnNextState;
	public Supplier<Tile> fnChasingTarget;
	public BooleanSupplier fnIsUnlocked;
	public int foodCount;

	public Ghost(PacManGame game, Maze maze, String name) {
		super(name, game, maze);
		steeringByState = new EnumMap<>(GhostState.class);
		defaultSteering = GhostSteerings.headingForTargetTile();
		fnNextState = this::getState;
		buildStateMachine();
	}

	@Override
	protected StateMachine<GhostState, PacManGameEvent> fsm() {
		if (fsm == null) {
			throw new IllegalStateException("Access to state machine failed, state machine not yet created");
		}
		return fsm;
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
	public boolean canCrossBorder(Tile current, Tile tile) {
		if (maze.isDoor(tile)) {
			return getState() == ENTERING_HOUSE || getState() == LEAVING_HOUSE;
		}
		if (maze.isNoUpIntersection(current) && tile == maze.tileToDir(current, Top4.N)) {
			return getState() != GhostState.CHASING && getState() != GhostState.SCATTERING;
		}
		return super.canCrossBorder(current, tile);
	}

	@Override
	/* TODO: Some values are still guessed */
	public float maxSpeed() {
		boolean inTunnel = maze.isTunnel(currentTile());
		switch (getState()) {
		case LOCKED:
			//$FALL-THROUGH$
		case LEAVING_HOUSE:
			//$FALL-THROUGH$
		case ENTERING_HOUSE:
			return game.speed(GHOST_SPEED) / 2;
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return inTunnel ? game.speed(GHOST_TUNNEL_SPEED) : game.speed(GHOST_SPEED);
		case FRIGHTENED:
			return inTunnel ? game.speed(GHOST_TUNNEL_SPEED) : game.speed(GHOST_FRIGHTENED_SPEED);
		case DYING:
			return 0;
		case DEAD:
			return 2 * game.speed(GHOST_SPEED);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), name));
		}
	}

	private void reverseDirection() {
		enteredNewTile = true;
		int oppositeDir = NESW.inv(moveDir);
		IntStream.of(oppositeDir, NESW.left(oppositeDir), NESW.right(oppositeDir)).filter(this::canCrossBorderTo)
				.findFirst().ifPresent(this::setNextDir);
	}

	private boolean unlocked() {
		return fnIsUnlocked.getAsBoolean();
	}

	private boolean inHouse() {
		return maze.inGhostHouse(currentTile()) || maze.isDoor(currentTile());
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
					.onTick(() -> {
						steer();
						move();
						sprites.select("color-" + moveDir);
					})
					.onExit(() -> {
						game.pacMan.ticksSinceLastMeal = 0;
					})
					
				.state(LEAVING_HOUSE)
					.onTick(() -> {
						if (inHouse()) {
							targetTile = maze.blinkyHome;
						}
						enteredNewTile = true; //TODO this workaround avoids getting stuck in house
						steer();
						move();
						sprites.select("color-" + moveDir);
					})
					.onExit(() -> moveDir = nextDir = Top4.W)
				
				.state(ENTERING_HOUSE)
				  .onEntry(() -> targetTile = revivalTile)
					.onTick(() -> {
						steer();
						move();
						sprites.select("eyes-" + moveDir);
					})
				
				.state(SCATTERING)
					.onEntry(() -> targetTile = scatterTile)
					.onTick(() -> {
						steer();
						move();
						sprites.select("color-" + moveDir);
					})
			
				.state(CHASING)
					.onEntry(this::chasingSoundOn)
					.onTick(() -> {
						targetTile = fnChasingTarget.get();
						steer();
						move();
						sprites.select("color-" + moveDir);
					})
					.onExit(this::chasingSoundOff)
				
				.state(FRIGHTENED)
					.onEntry(this::reverseDirection)
					.onTick(() -> {
						steer();
						move();
						sprites.select(game.pacMan.isLosingPower()	? "flashing" : "frightened");
					})
				
				.state(DYING)
					.timeoutAfter(Ghost::getDyingTime)
					.onEntry(() -> {
						sprites.select("value-" + game.numGhostsKilledByCurrentEnergizer()); 
					})
					.onExit(game::addGhostKilled)
				
				.state(DEAD)
					.onEntry(() -> {
						targetTile = maze.blinkyHome;
						deadSoundOn();
					})
					.onTick(() -> {
						steer();
						move();
						sprites.select("eyes-" + moveDir);
					})
					.onExit(this::deadSoundOff)
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE).condition(this::unlocked)
			
				.when(LEAVING_HOUSE).then(FRIGHTENED)
					.condition(() -> !inHouse() && game.pacMan.hasPower())

				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> !inHouse() && getNextState() == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> !inHouse() && getNextState() == CHASING)
					
				.when(ENTERING_HOUSE).then(LOCKED)
					.condition(() -> currentTile() == targetTile)
				
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
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> currentTile().equals(maze.blinkyHome))
				
		.endStateMachine();
		/*@formatter:on*/
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}
}