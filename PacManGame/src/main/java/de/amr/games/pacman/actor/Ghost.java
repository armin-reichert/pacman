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

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.actor.steering.ghost.SteeredGhost;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
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

	public int seat;
	public GhostState followState;
	private Steering prevSteering;

	public Ghost(Game game, String name) {
		super(game, name);
		steerings = new EnumMap<>(GhostState.class);
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(Ghost.this::toString)
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						visible = true;
						setWishDir(maze().ghostHomeDir[seat]);
						setMoveDir(wishDir());
						tf.setPosition(maze().seatPosition(seat));
						enteredNewTile();
						sprites.select("color-" + moveDir());
						sprites.forEach(Sprite::resetAnimation);
						followState = getState();
					})
					.onTick(() -> {
							move(game.pacMan.powerTicks > 0 ? "frightened" : "color-" + moveDir());
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(() -> move("color-" + moveDir()))
					.onExit(() -> forceMoving(LEFT))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> {
						tf.setPosition(maze().seatPosition(0));
						setWishDir(DOWN);
						steering().init();
					})
					.onTick(() -> move("eyes-" + moveDir()))
				
				.state(SCATTERING)
					.onTick(() -> {
						move("color-" + moveDir());
						checkPacManCollision();
					})
			
				.state(CHASING)
					.onTick(() -> {
						move("color-" + moveDir());
						checkPacManCollision();
					})
				
				.state(FRIGHTENED)
					.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
					.onTick((state, t, remaining) -> {
						move(remaining < sec(2) ? "flashing" : "frightened");
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
							move("eyes-" + moveDir());
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.stay(LOCKED)
					.on(PacManGainsPowerEvent.class)
					
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> steering().isComplete() && followState == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> steering().isComplete() && followState == CHASING)
				
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
					
				.stay(DEAD)
					.on(PacManGainsPowerEvent.class)
				
		.endStateMachine();
		/*@formatter:on*/
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.getTracer().setLogger(PacManStateMachineLogging.LOG);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (maze().isUpwardsBlocked(tile) && neighbor.equals(maze().neighbor(tile, UP))) {
			return !is(CHASING, SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	public float speed(Tile tile, GhostState state) {
		switch (state) {
		case LOCKED:
			return maze().insideGhostHouse(tile) ? relSpeed(game.level.ghostSpeed) / 2 : 0;
		case LEAVING_HOUSE:
			return relSpeed(game.level.ghostSpeed) / 2;
		case ENTERING_HOUSE:
			return relSpeed(game.level.ghostSpeed);
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return maze().isTunnel(tile) ? relSpeed(game.level.ghostTunnelSpeed) : relSpeed(game.level.ghostSpeed);
		case FRIGHTENED:
			return maze().isTunnel(tile) ? relSpeed(game.level.ghostTunnelSpeed) : relSpeed(game.level.ghostFrightenedSpeed);
		case DEAD:
			return 2 * relSpeed(game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", state, name));
		}
	}

	private void move(String spriteKey) {
		Steering currentSteering = steering();
		if (prevSteering != currentSteering) {
			loginfo("%s steering changed from %s to %s", this, Steering.name(prevSteering), Steering.name(currentSteering));
			currentSteering.init();
			currentSteering.force();
			prevSteering = currentSteering;
		}
		currentSteering.steer();
		movement.update();
		sprites.select(spriteKey);
	}

	private void checkPacManCollision() {
		if (isTeleporting() || game.pacMan.isTeleporting() || game.pacMan.is(PacManState.DEAD)) {
			return;
		}
		if (tile().equals(game.pacMan.tile())) {
			publish(new PacManGhostCollisionEvent(this));
		}
	}
}