package de.amr.games.pacman.controller.actor;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Game.sec;
import static de.amr.games.pacman.model.Game.speed;

import java.util.EnumMap;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.EnteringGhostHouse;
import de.amr.games.pacman.controller.actor.steering.ghost.FleeingToSafeCorner;
import de.amr.games.pacman.controller.actor.steering.ghost.JumpingUpAndDown;
import de.amr.games.pacman.controller.actor.steering.ghost.LeavingGhostHouse;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * <p>
 * Ghosts are creatures with additional behaviors like entering and leaving the ghost house or
 * jumping up and down at some position.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature<GhostState> {

	public enum Insanity {
		IMMUNE, SANE, CRUISE_ELROY1, CRUISE_ELROY2
	};

	/** Tile headed for when ghost scatters out. */
	public Tile scatteringTarget;

	/** State to enter after frightening state ends. */
	public GhostState subsequentState;

	/** Keeps track of steering changes. */
	public Steering previousSteering;

	/** Insanity ("cruise elroy") level. */
	public Insanity insanity = Insanity.IMMUNE;

	public Ghost(Game game, String name) {
		super(game, name, new EnumMap<>(GhostState.class));
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(this::toString)
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						subsequentState = LOCKED;
						visible = true;
						if (insanity != Insanity.IMMUNE) {
							insanity = Insanity.SANE;
						}
						moveDir = wishDir = seat.startDir;
						tf.setPosition(seat.position);
						enteredNewTile();
						sprites.forEach(Sprite::resetAnimation);
						showColored();
					})
					.onTick(() -> {
						move();
						// not sure if ghost locked inside house should look frightened
						if (game.pacMan.power > 0) {
							showFrightened();
						} else {
							showColored();
						}
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> {
						steering().init();
					})
					.onTick(() -> {
						move();
						showColored();
					})
					.onExit(() -> forceMoving(Direction.LEFT))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> {
						tf.setPosition(maze.ghostSeats[0].position);
						moveDir = wishDir = Direction.DOWN;
						steering().init();
					})
					.onTick(() -> {
						move();
						showEyes();
					})
				
				.state(SCATTERING)
					.onTick(() -> {
						updateInsanity();
						move();
						showColored();
						checkPacManCollision();
					})
			
				.state(CHASING)
					.onTick(() -> {
						updateInsanity();
						move();
						showColored();
						checkPacManCollision();
					})
				
				.state(FRIGHTENED)
					.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
					.onTick((state, t, remaining) -> {
						move();
						// one flashing animation takes 0.5 sec
						int flashTicks = sec(game.level.numFlashes * 0.5f);
						if (remaining < flashTicks) {
							showFlashing();
						} else  {
							showFrightened();
						}
						checkPacManCollision();
					})
				
				.state(DEAD)
					.timeoutAfter(sec(1)) // time while ghost is drawn as number of scored points
					.onEntry(() -> {
						showPoints(Game.POINTS_GHOST[game.level.ghostsKilledByEnergizer - 1]);
					})
					.onTick((state, t, remaining) -> {
						if (remaining == 0) { // show as eyes returning to ghost home
							move();
							showEyes();
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> steering().isComplete() && subsequentState == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> steering().isComplete() && subsequentState == CHASING)
				
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> subsequentState == SCATTERING)
					.act(() -> reverseDirection())
					
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> subsequentState == CHASING)
					.act(() -> reverseDirection())
					
				.stay(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> restartTimer(FRIGHTENED))
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> subsequentState == SCATTERING)
					
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> subsequentState == CHASING)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> maze.atGhostHouseDoor(tile()))
					
		.endStateMachine();
		/*@formatter:on*/
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		brain.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	/**
	 * @return steering which lets the ghost jump up and down in its seat
	 */
	public Steering isBouncingOnSeat() {
		return new JumpingUpAndDown(this, seat.position.y);
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * @return steering where actor flees to a "safe" maze corner
	 */
	public Steering isFleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner(this, attacker, game.maze.cornerNW, game.maze.cornerNE, game.maze.cornerSW,
				game.maze.cornerSE);
	}

	/**
	 * @return steering for bringing ghost back to ghost house entry
	 */
	public Steering isReturningToHouse() {
		return isHeadingFor(maze.ghostSeats[0].tile);
	}

	/**
	 * @return steering for letting ghost scattering out and circling in its corner
	 */
	public Steering isScatteringOut() {
		return isHeadingFor(scatteringTarget);
	}

	/**
	 * @return steering which lets ghost enter the house and taking its seat
	 */
	public Steering isTakingSeat() {
		return new EnteringGhostHouse(this, Vector2f.of(seat.position.x, seat.position.y + 3));
	}

	/**
	 * @return steering which lets a ghost leave the ghost house
	 */
	public Steering isLeavingGhostHouse() {
		return new LeavingGhostHouse(this);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (maze.isOneWayDown(tile) && neighbor.equals(maze.neighbor(tile, UP))) {
			return !is(CHASING, SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	public void move() {
		Steering currentSteering = steering();
		if (previousSteering != currentSteering) {
			PacManStateMachineLogging.loginfo("%s steering changed from %s to %s", this, Steering.name(previousSteering),
					Steering.name(currentSteering));
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		currentSteering.steer();
		movement.update();
	}

	@Override
	public float currentSpeed(Game game) {
		switch (getState()) {
		case LOCKED:
			return speed(maze.insideGhostHouse(tile()) ? game.level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(game.level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(game.level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (maze.isTunnel(tile())) {
				return speed(game.level.ghostTunnelSpeed);
			} else {
				switch (insanity) {
				case CRUISE_ELROY2:
					return speed(game.level.elroy2Speed);
				case CRUISE_ELROY1:
					return speed(game.level.elroy1Speed);
				case SANE:
				case IMMUNE:
					return speed(game.level.ghostSpeed);
				default:
					throw new IllegalArgumentException("Illegal ghost state: " + getState());
				}
			}
		case FRIGHTENED:
			return speed(maze.isTunnel(tile()) ? game.level.ghostTunnelSpeed : game.level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", getState()));
		}
	}

	public void takeClothes(Theme theme, int color) {
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, theme.spr_ghostColored(color, dir));
			sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		sprites.set("frightened", theme.spr_ghostFrightened());
		sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			sprites.set("points-" + points, theme.spr_number(points));
		}
	}

	public void showColored() {
		sprites.select("color-" + moveDir);
	}

	public void showFrightened() {
		sprites.select("frightened");
	}

	public void showEyes() {
		sprites.select("eyes-" + moveDir);
	}

	public void showFlashing() {
		sprites.select("flashing");
	}

	public void showPoints(int points) {
		sprites.select("points-" + points);
	}

	private void checkPacManCollision() {
		if (tile().equals(game.pacMan.tile()) && !isTeleporting() && !game.pacMan.isTeleporting()
				&& !game.pacMan.is(PacManState.DEAD)) {
			publish(new PacManGhostCollisionEvent(this));
		}
	}

	private void updateInsanity() {
		if (insanity == Insanity.IMMUNE) {
			return;
		}
		int pelletsLeft = game.remainingFoodCount();
		Insanity oldInsanity = insanity;
		if (pelletsLeft <= game.level.elroy2DotsLeft) {
			insanity = Insanity.CRUISE_ELROY2;
		} else if (pelletsLeft <= game.level.elroy1DotsLeft) {
			insanity = Insanity.CRUISE_ELROY1;
		}
		if (oldInsanity != insanity) {
			loginfo("%s's insanity changed from %s to %s, pellets left: %d", name, oldInsanity, insanity, pelletsLeft);
		}
	}
}