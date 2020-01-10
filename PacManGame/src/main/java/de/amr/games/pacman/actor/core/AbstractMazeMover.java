package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.actor.core.MazeMover.MoveState.MOVING;
import static de.amr.games.pacman.actor.core.MazeMover.MoveState.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.Objects;

import de.amr.easy.game.entity.Entity;
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
public abstract class AbstractMazeMover extends Entity implements SteerableMazeMover {

	private final String name;
	protected final StateMachine<MoveState, Void> movement;
	private Direction moveDir;
	private Direction wishDir;
	private Tile targetTile;
	private boolean enteredNewTile;
	private int teleportingTicks;

	/**
	 * @return maximum distance (in pixels) this entity can move in the next step
	 */
	protected abstract float maxSpeed();

	public AbstractMazeMover(String name) {
		this.name = name;
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
		movement = new StateMachine<MoveState, Void>(MoveState.class) {

			{
				//@formatter:off
				beginStateMachine()
					.description(String.format("[%s movement]", name))
					.initialState(MOVING)
					.states()
						.state(MOVING)
						.onEntry(() -> setVisible(true))
							.onTick(() -> moveInsideMaze())
						.state(TELEPORTING)
							.timeoutAfter(() -> teleportingTicks)
							.onEntry(() -> setVisible(false))
							.onExit(() -> setVisible(true))
					.transitions()
						.when(MOVING).then(TELEPORTING)
							.condition(() -> enteredPortal())
							.act(() -> traversePortal())
						.when(TELEPORTING).then(MOVING)
							.onTimeout()
				.endStateMachine();
				//@formatter:on
			}
		};
		movement.setLogger(Game.FSM_LOGGER);
	}

	public void init() {
		moveDir = wishDir = RIGHT;
		targetTile = null;
		enteredNewTile = true;
		movement.init();
	}

	public String name() {
		return name;
	}

	@Override
	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}

	/**
	 * Places this entity at the given tile with given pixel offsets.
	 * 
	 * @param tile    tile
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tile.equals(tile());
	}

	/**
	 * Places this entity exactly over the given tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	public void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	/**
	 * Places this entity between the given tile and its right neighbor tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	public void placeHalfRightOf(Tile tile) {
		placeAt(tile, Tile.SIZE / 2, 0);
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

	public void forceMove(Direction dir) {
		wishDir = dir;
		steering().force();
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

	private void traversePortal() {
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
}