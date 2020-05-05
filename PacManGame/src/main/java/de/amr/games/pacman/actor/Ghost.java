package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Timing.relSpeed;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.actor.steering.ghost.SteeredGhost;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.tiles.Tile;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * <p>
 * Ghosts have a "brain" (finite-state machine) defining the ghost's behavior
 * (steering, look).
 * 
 * @author Armin Reichert
 */
public class Ghost extends MovingActor<GhostState> implements SteeredGhost {

	public SpriteMap sprites = new SpriteMap();
	public GhostState followState;
	public int seat;
	private Steering prevSteering;
	private Fsm<GhostState, PacManGameEvent> brain;
	private Map<GhostState, Steering> steerings = new EnumMap<>(GhostState.class);

	public Ghost(Game game, String name) {
		super(game, name);
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(Ghost.this::toString)
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						takeSeat();
						visible = true;
						followState = getState();
						sprites.select("color-" + moveDir());
						sprites.forEach(Sprite::resetAnimation);
					})
					.onTick(() -> {
							step(game.pacMan.powerTicks > 0 ? "frightened" : "color-" + moveDir());
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(() -> step("color-" + moveDir()))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(() -> step("eyes-" + moveDir()))
				
				.state(SCATTERING)
					.onTick(() -> {
						step("color-" + moveDir());
						checkPacManCollision();
					})
			
				.state(CHASING)
					.onTick(() -> {
						step("color-" + moveDir());
						checkPacManCollision();
					})
				
				.state(FRIGHTENED)
					.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
					.onTick((state, t, remaining) -> {
						step(remaining < sec(2) ? "flashing" : "frightened");
						checkPacManCollision();
					})
				
				.state(DEAD)
					.timeoutAfter(sec(1)) // "dying" time
					.onEntry(() -> {
						int points = Game.POINTS_GHOST[game.level.ghostsKilledByEnergizer - 1];
						sprites.select("points-" + points);
					})
					.onTick(() -> {
						if (state().isTerminated()) { // "dead"
							step("eyes-" + moveDir());
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.stay(LOCKED)
					.on(PacManGainsPowerEvent.class)
					
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> steering().isComplete() && followState == SCATTERING)
					.act(() -> forceMoving(LEFT))
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> steering().isComplete() && followState == CHASING)
					.act(() -> forceMoving(LEFT))
				
				.stay(LEAVING_HOUSE)
					.on(PacManGainsPowerEvent.class)
				
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
				
				.stay(ENTERING_HOUSE)
					.on(PacManGainsPowerEvent.class)
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceTurningBack())
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> followState == SCATTERING)
					.act(() -> forceTurningBack())
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceTurningBack())
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> followState == CHASING)
					.act(() -> forceTurningBack())
				
				.stay(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> restartTimer(FRIGHTENED))
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> followState == SCATTERING)
					
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> followState == CHASING)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> maze().atGhostHouseDoor(tile()))
					.act(() -> {
						tf.setPosition(maze().seatPosition(0));
						setWishDir(DOWN);
					})
					
				.stay(DEAD)
					.on(PacManGainsPowerEvent.class)
				
		.endStateMachine();
		/*@formatter:on*/
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.getTracer().setLogger(PacManStateMachineLogging.LOG);
	}

	@Override
	public Fsm<GhostState, PacManGameEvent> fsm() {
		return brain;
	}

	@Override
	public void init() {
		super.init();
		brain.init();
	}

	public void takeSeat() {
		tf.setPosition(maze().seatPosition(seat));
		setMoveDir(maze().ghostHouseSeatDirs[seat]);
		setWishDir(maze().ghostHouseSeatDirs[seat]);
		enteredNewTile();
	}

	public void behavior(GhostState state, Steering steering) {
		steerings.put(state, steering);
	}

	public Steering steering(GhostState state) {
		if (steerings.containsKey(state)) {
			return steerings.get(state);
		}
		throw new IllegalArgumentException(String.format("%s: No steering found for state %s", this, state));
	}

	@Override
	public Steering steering() {
		return steering(getState());
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (maze().isNoUpIntersection(tile) && neighbor == maze().tileToDir(tile, UP)) {
			return !is(CHASING, SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	public float speed() {
		switch (getState()) {
		case LOCKED:
			return maze().insideGhostHouse(tile()) ? relSpeed(game.level.ghostSpeed) / 2 : 0;
		case LEAVING_HOUSE:
			return relSpeed(game.level.ghostSpeed) / 2;
		case ENTERING_HOUSE:
			return relSpeed(game.level.ghostSpeed);
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return maze().isTunnel(tile()) ? relSpeed(game.level.ghostTunnelSpeed) : relSpeed(game.level.ghostSpeed);
		case FRIGHTENED:
			return maze().isTunnel(tile()) ? relSpeed(game.level.ghostTunnelSpeed)
					: relSpeed(game.level.ghostFrightenedSpeed);
		case DEAD:
			return 2 * relSpeed(game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), name));
		}
	}

	private void step(String spriteKey) {
		if (isTeleporting()) {
			move();
		} else {
			if (prevSteering != steering()) {
				steering().init();
				steering().force();
				loginfo("%s steering changed from %s to %s", this, name(prevSteering), name(steering()));
				prevSteering = steering();
			}
			steering().steer();
			move();
			sprites.select(spriteKey);
		}
	}

	private void forceMoving(Direction dir) {
		setWishDir(dir);
		move();
	}

	private void forceTurningBack() {
		forceMoving(moveDir().opposite());
	}

	private String name(Steering steering) {
		return steering != null ? steering.getClass().getSimpleName() : "no steering";
	}

	private void checkPacManCollision() {
		if (isTeleporting() || game.pacMan.isTeleporting()) {
			return;
		}
		if (tile().equals(game.pacMan.tile()) && game.pacMan.is(PacManState.EATING)) {
			publish(new PacManGhostCollisionEvent(this, tile()));
		}
	}
}