package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.actor.core.MovingActor.Movement.MOVING_INSIDE_MAZE;
import static de.amr.games.pacman.actor.core.MovingActor.Movement.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.Objects;

import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.steering.MazeMover;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Base class for all moving actors (ghosts, Pac-Man).
 * 
 * @param <S>
 *          state identifier type
 * 
 * @author Armin Reichert
 */
public abstract class MovingActor<S> extends Actor<S> implements MazeMover {

	enum Movement {
		MOVING_INSIDE_MAZE, TELEPORTING;
	}

	private final StateMachine<Movement, ?> movement;
	private Direction moveDir;
	private Direction wishDir;
	private Tile targetTile;
	private boolean enteredNewTile;
	private int teleportingTicks;

	public MovingActor(Cast cast, String name) {
		super(cast, name);
		movement = buildMovementControl();
		movement.setLogger(Game.FSM_LOGGER);
	}

	private StateMachine<Movement, ?> buildMovementControl() {
		StateMachine<Movement, Void> fsm = StateMachine
		//@formatter:off
			.beginStateMachine(Movement.class, Void.class)
				.description(String.format("[%s movement]", name()))
				.initialState(MOVING_INSIDE_MAZE)
				.states()
					.state(MOVING_INSIDE_MAZE)
						.onTick(() -> makeStepInsideMaze())
					.state(TELEPORTING)
						.timeoutAfter(() -> teleportingTicks)
						.onEntry(() -> setVisible(false))
						.onExit(() -> setVisible(true))
				.transitions()
					.when(MOVING_INSIDE_MAZE).then(TELEPORTING)
						.condition(() -> enteredLeftPortal() || enteredRightPortal())
					.when(TELEPORTING).then(MOVING_INSIDE_MAZE)
						.onTimeout()
						.act(() -> teleport())
			.endStateMachine();
		//@formatter:on
		return fsm;
	}

	public abstract Steering steering();

	protected abstract float speed();

	@Override
	public void init() {
		moveDir = wishDir = RIGHT;
		targetTile = null;
		enteredNewTile = true;
		movement.init();
	}

	public void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	public void placeAt(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tile.equals(tile());
	}

	public void setTeleportingDuration(int ticks) {
		teleportingTicks = ticks;
	}

	@Override
	public boolean enteredNewTile() {
		return enteredNewTile;
	}

	@Override
	public Direction moveDir() {
		return moveDir;
	}

	@Override
	public void setMoveDir(Direction dir) {
		moveDir = Objects.requireNonNull(dir);
	}

	@Override
	public Direction wishDir() {
		return wishDir;
	}

	@Override
	public void setWishDir(Direction dir) {
		wishDir = dir;
	}

	@Override
	public Tile targetTile() {
		return targetTile;
	}

	@Override
	public void setTargetTile(Tile tile) {
		targetTile = tile;
	}

	@Override
	public boolean isTeleporting() {
		return movement.is(TELEPORTING);
	}

	@Override
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile(), neighbor = maze().tileToDir(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (neighbor.isWall()) {
			return false;
		}
		if (neighbor.isTunnel()) {
			return true; // includes portal tiles
		}
		return maze().insideBoard(neighbor);
	}

	protected void move() {
		movement.update();
	}

	/**
	 * Computes how many pixels this entity can move towards the given direction without crossing the border to a
	 * forbidden neighbor tile.
	 */
	private float possibleMoveDistance(Tile currentTile, Direction dir) {
		float dist = speed();
		if (canCrossBorderTo(dir)) {
			return dist;
		}
		float offsetX = tf.getX() - currentTile.x(), offsetY = tf.getY() - currentTile.y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, dist);
		case DOWN:
			return Math.min(-offsetY, dist);
		case LEFT:
			return Math.min(offsetX, dist);
		case RIGHT:
			return Math.min(-offsetX, dist);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	private void makeStepInsideMaze() {
		Tile tile = tile();
		float speed = possibleMoveDistance(tile, moveDir);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = possibleMoveDistance(tile, wishDir);
			if (wishDirSpeed > 0) {
				boolean corner = (wishDir == moveDir.turnLeft() || wishDir == moveDir.turnRight());
				if (corner && steering().requiresGridAlignment()) {
					placeAt(tile);
				}
				moveDir = wishDir;
				speed = wishDirSpeed;
			}
		}
		tf.setVelocity(speed * moveDir.dx, speed * moveDir.dy);
		tf.move();
		enteredNewTile = !tile.equals(tile());
	}

	private void teleport() {
		if (enteredRightPortal()) {
			placeAt(maze().portalLeft);
		}
		else if (enteredLeftPortal()) {
			placeAt(maze().portalRight);
		}
	}

	private boolean enteredLeftPortal() {
		return tf.getPosition().x < maze().portalLeft.x();
	}

	private boolean enteredRightPortal() {
		return tf.getPosition().x > maze().portalRight.x();
	}
}