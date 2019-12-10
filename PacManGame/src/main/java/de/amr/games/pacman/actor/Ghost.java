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
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.speed;
import static de.amr.graph.grid.impl.Grid4Topology.N;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.common.Steerings;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeMover implements Actor<GhostState> {

	private final Map<GhostState, Steering<Ghost>> steeringByState;
	private final Steering<Ghost> defaultSteering;

	public final PacManGameCast cast;
	public final PacManGame game;
	public final ActorPrototype<GhostState> _actor;
	public int initialDir;
	public Tile initialTile;
	public Tile revivalTile;
	public Tile scatterTile;
	public GhostState nextState;
	public Supplier<Tile> fnChasingTarget;
	public int foodCount;

	public Ghost(String name, PacManGameCast cast) {
		super(cast.game.maze);
		this.cast = cast;
		this.game = cast.game;
		steeringByState = new EnumMap<>(GhostState.class);
		defaultSteering = Steerings.headingForTargetTile();
		_actor = buildActorComponent(name); 
	}

	private ActorPrototype<GhostState> buildActorComponent(String name) {
		StateMachine<GhostState, PacManGameEvent> fsm = buildStateMachine(name);
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		return new ActorPrototype<>(name, fsm);
	}

	private StateMachine<GhostState, PacManGameEvent> buildStateMachine(String name) {
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
						cast.pacMan.ticksSinceLastMeal = 0;
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> targetTile = maze.blinkyHome)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir))
					.onExit(() -> moveDir = nextDir = W)
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> targetTile = revivalTile)
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))
				
				.state(SCATTERING)
					.onEntry(() -> targetTile = scatterTile)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir))
			
				.state(CHASING)
					.onEntry(() -> cast.chasingSoundOn())
					.onTick(() -> {
						targetTile = fnChasingTarget.get();
						walkAndDisplayAs("color-" + moveDir);
					})
					.onExit(() -> cast.chasingSoundOff(this))
				
				.state(FRIGHTENED)
					.onTick(() -> walkAndDisplayAs(cast.pacMan.isLosingPower() ? "flashing" : "frightened"))
				
				.state(DYING)
					.timeoutAfter(Ghost::getDyingTime)
					.onEntry(() -> {
						sprites.select("value-" + game.level.bodyCount);
					})
				
				.state(DEAD)
					.onEntry(() -> {
						targetTile = maze.blinkyHome;
						cast.deadSoundOn();
					})
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))
					.onExit(() -> cast.deadSoundOff(this))
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> leftHouse() && nextState == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> leftHouse() && nextState == CHASING)
					
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
					.condition(() -> nextState == CHASING)
	
				.when(FRIGHTENED).then(SCATTERING)
					.on(PacManLostPowerEvent.class)
					.condition(() -> nextState == SCATTERING)
				
				.when(FRIGHTENED).then(DYING)
					.on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD)
					.onTimeout()
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> tile().equals(maze.blinkyHome))
				
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public Actor<GhostState> _actor() {
		return _actor;
	}

	@Override
	public void activate() {
		_actor.activate();
		init();
		show();
	}

	@Override
	public void deactivate() {
		_actor.deactivate();
		hide();
	}

	@Override
	public void init() {
		super.init();
		_actor.init();
		visible = true;
		moveDir = initialDir;
		nextDir = initialDir;
		placeAtTile(initialTile, Maze.TS / 2, 0);
		sprites.select("color-" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
		nextState = _actor.getState();
	}

	@Override
	public void update() {
		super.update();
		_actor.update();
	}

	public void setSteering(GhostState state, Steering<Ghost> steering) {
		steeringByState.put(state, steering);
	}

	public Steering<Ghost> getSteering() {
		return steeringByState.getOrDefault(_actor.getState(), defaultSteering);
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
		if (maze.isNoUpIntersection(tile) && neighbor == maze.tileToDir(tile, N)) {
			return getState() != CHASING && getState() != SCATTERING;
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	/* TODO: Some values are still guessed */
	public float maxSpeed() {
		boolean inTunnel = maze.isTunnel(tile());
		boolean outsideHouse = !maze.inGhostHouse(tile());
		switch (getState()) {
		case LOCKED:
			return outsideHouse ? 0 : speed(game.level.ghostSpeed) / 2;
		case LEAVING_HOUSE:
			//$FALL-THROUGH$
		case ENTERING_HOUSE:
			return speed(game.level.ghostSpeed) / 2;
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return inTunnel ? speed(game.level.ghostTunnelSpeed) : speed(game.level.ghostSpeed);
		case FRIGHTENED:
			return inTunnel ? speed(game.level.ghostTunnelSpeed) : speed(game.level.ghostFrightenedSpeed);
		case DYING:
			return 0;
		case DEAD:
			return 2 * speed(game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), _actor.name));
		}
	}

	private void walkAndDisplayAs(String spriteKey) {
		steer();
		move();
		sprites.select(spriteKey);
	}

	private boolean leftHouse() {
		Tile currentTile = tile();
		return !maze.partOfGhostHouse(currentTile) && tf.getY() - currentTile.row * Maze.TS == 0;
	}

	public static int getDyingTime() {
		return sec(1);
	}
}