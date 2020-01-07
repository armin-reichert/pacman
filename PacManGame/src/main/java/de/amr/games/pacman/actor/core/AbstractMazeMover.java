package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.actor.core.MazeMover.MoveState.MOVING;
import static de.amr.games.pacman.actor.core.MazeMover.MoveState.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.Objects;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.steering.common.SteerableMazeMover;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Base class for entities that can move through the maze.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractMazeMover extends AbstractMazeResident implements SteerableMazeMover {

	private final StateMachine<MoveState, Void> movement = new StateMachine<MoveState, Void>(MoveState.class) {

		{
			//@formatter:off
			beginStateMachine()
				.description(String.format("[%s movement]", name()))
				.initialState(MOVING)
				.states()
					.state(MOVING)
						.onTick(() -> moveInsideMaze())
					.state(TELEPORTING)
						.timeoutAfter(() -> teleportingTicks)
						.onEntry(() -> setVisible(false))
						.onExit(() -> setVisible(true))
				.transitions()
					.when(MOVING).then(TELEPORTING)
						.condition(() -> enteredPortal())
						.act(() -> placeAtPortalExit())
					.when(TELEPORTING).then(MOVING)
						.onTimeout()
			.endStateMachine();
			//@formatter:on
		}
	};

	private Direction moveDir;
	private Direction wishDir;
	private Tile targetTile;
	private boolean enteredNewTile;
	private int teleportingTicks;

	/**
	 * @return maximum distance (in pixels) this entity can move in the next step
	 */
	protected abstract float maxSpeed();

	public abstract String name();

	public AbstractMazeMover() {
		movement.setLogger(Game.FSM_LOGGER);
	}

	public void init() {
		moveDir = wishDir = RIGHT;
		targetTile = null;
		enteredNewTile = true;
		movement.init();
	}

	protected void moveOneStep() {
		movement.update();
	}

	/**
	 * Computes how many pixels this entity can move towards the given direction
	 * without crossing the border to a forbidden neighbor tile.
	 */
	private float possibleSpeed(Tile currentTile, Direction dir) {
		float maxSpeed = maxSpeed();
		if (canCrossBorderTo(dir)) {
			return maxSpeed;
		}
		float offsetX = tf.getX() - currentTile.x(), offsetY = tf.getY() - currentTile.y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, maxSpeed);
		case DOWN:
			return Math.min(-offsetY, maxSpeed);
		case LEFT:
			return Math.min(offsetX, maxSpeed);
		case RIGHT:
			return Math.min(-offsetX, maxSpeed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	private void moveInsideMaze() {
		Tile tileBeforeStep = tile();
		float speed = possibleSpeed(tileBeforeStep, moveDir);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = possibleSpeed(tileBeforeStep, wishDir);
			if (wishDirSpeed > 0) {
				boolean turning = (wishDir == moveDir.turnLeft() || wishDir == moveDir.turnRight());
				if (turning && steering().requiresGridAlignment()) {
					tf.setPosition(tileBeforeStep.x(), tileBeforeStep.y());
				}
				moveDir = wishDir;
				speed = wishDirSpeed;
			}
		}
		tf.setVelocity(Vector2f.smul(speed, Vector2f.of(moveDir.dx, moveDir.dy)));
		tf.move();
		enteredNewTile = !tileBeforeStep.equals(tile());
	}

	private void placeAtPortalExit() {
		Tile tile = tile();
		if (tile.equals(maze().portalLeft)) {
			placeAt(maze().portalRight);
		} else if (tile.equals(maze().portalRight)) {
			placeAt(maze().portalLeft);
		}
	}

	private boolean enteredPortal() {
		return enteredNewTile() && maze().isPortal(tile());
	}

	public void forceMove(Direction dir) {
		wishDir = dir;
		steering().force();
		movement.update();
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

	@Override
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		super.placeAt(tile, xOffset, yOffset);
		enteredNewTile = !tile.equals(tile());
	}
}