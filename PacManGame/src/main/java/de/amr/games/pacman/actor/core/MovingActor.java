package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.actor.core.MovingActor.MoveState.MOVING;
import static de.amr.games.pacman.actor.core.MovingActor.MoveState.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.Objects;

import de.amr.games.pacman.actor.Cast;
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

	enum MoveState {
		MOVING, TELEPORTING;
	}

	private final StateMachine<MoveState, ?> movement;
	private Direction moveDir;
	private Direction wishDir;
	private Tile targetTile;
	private boolean enteredNewTile;
	private int teleportingTicks;

	public MovingActor(Cast cast, String name) {
		super(cast, name);
		movement = new StateMachine<MoveState, Void>(MoveState.class) {

			{
				//@formatter:off
				beginStateMachine()
					.description(String.format("[%s movement]", name))
					.initialState(MOVING)
					.states()
						.state(MOVING)
							.onTick(() -> makeStepInsideMaze())
						.state(TELEPORTING)
							.timeoutAfter(() -> teleportingTicks)
							.onEntry(() -> setVisible(false))
							.onExit(() -> setVisible(true))
					.transitions()
						.when(MOVING).then(TELEPORTING)
							.condition(() -> enteredLeftPortal() || enteredRightPortal())
						.when(TELEPORTING).then(MOVING)
							.onTimeout()
							.act(() -> teleport())
				.endStateMachine();
				//@formatter:on
			}
		};
		movement.setLogger(Game.FSM_LOGGER);
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

	public void placeAt(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tile.equals(tile());
	}

	public void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
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

	protected void forceMove(Direction dir) {
		if (canCrossBorderTo(dir)) {
			wishDir = dir;
			steering().force();
			move();
		}
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