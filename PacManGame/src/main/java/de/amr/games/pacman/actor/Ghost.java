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
import static de.amr.games.pacman.model.PacManGame.relSpeed;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_FRIGHTENED_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_SPEED;
import static de.amr.games.pacman.model.PacManGame.LevelData.GHOST_TUNNEL_SPEED;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
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

	public final Tile scatterTile;

	/** Function providing the next state after being FRIGHTENED or LOCKED. */
	public Supplier<GhostState> fnNextState;

	public Supplier<Tile> fnChasingTarget;

	public Function<Ghost, Boolean> fnIsUnlocked;

	public int foodCount;

	private StateMachine<GhostState, PacManGameEvent> fsm;

	private Map<GhostState, Steering> steeringInState = new EnumMap<>(GhostState.class);

	private final Steering defaultSteering = headingFor(() -> targetTile);

	public Ghost(PacManGame game, Maze maze, String name, GhostColor color, Tile initialTile, int initialDir,
			Tile scatterTile) {
		super(maze);
		this.game = game;
		this.name = name;
		this.initialTile = initialTile;
		this.initialDir = initialDir;
		this.scatterTile = scatterTile;
		this.nextDir = initialDir;
		this.moveDir = initialDir;
		fnNextState = this::getState;
		fnIsUnlocked = game::isUnlocked;
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
		steeringInState.getOrDefault(getState(), defaultSteering).steer(this);
	}

	@Override
	public boolean canEnterTile(Tile current, Tile tile) {
		if (maze.isDoor(tile)) {
			return getState() == ENTERING_HOUSE || getState() == LEAVING_HOUSE;
		}
		if (maze.isNoUpIntersection(current) && tile == maze.tileToDir(current, Top4.N)) {
			return getState() != GhostState.CHASING && getState() != GhostState.SCATTERING;
		}
		return super.canEnterTile(current, tile);
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
			return maze.inGhostHouse(tile) ? relSpeed(.25f) : 0;
		case LEAVING_HOUSE:
		case ENTERING_HOUSE:
			return relSpeed(.25f);
		case CHASING:
		case SCATTERING:
			return maze.isTunnel(tile) ? tunnelSpeed : relSpeed(GHOST_SPEED.$float(level));
		case FRIGHTENED:
			return maze.isTunnel(tile) ? tunnelSpeed : relSpeed(GHOST_FRIGHTENED_SPEED.$float(level));
		case DYING:
			return 0;
		case DEAD:
			return 2 * relSpeed(GHOST_SPEED.$float(level));
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), name));
		}
	}

	private void reverseDirection() {
		int oppositeDir = NESW.inv(moveDir);
		IntStream.of(oppositeDir, NESW.left(oppositeDir), NESW.right(oppositeDir)).filter(this::canEnterTileTo)
				.findFirst().ifPresent(dir -> nextDir = dir);
	}

	private boolean unlocked() {
		return fnIsUnlocked.apply(this);
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
						enteredNewTile = true;
					})
					
				.state(LEAVING_HOUSE)
					.onTick(() -> {
						if (inHouse()) {
							targetTile = maze.blinkyHome;
						}
						steer();
						move();
						sprites.select("color-" + moveDir);
					})
					.onExit(() -> moveDir = nextDir = Top4.W)
				
				.state(ENTERING_HOUSE)
				  .onEntry(() -> targetTile = maze.pinkyHome)
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