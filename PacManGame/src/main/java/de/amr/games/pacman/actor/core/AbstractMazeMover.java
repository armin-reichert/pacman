package de.amr.games.pacman.actor.core;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Base class for maze movers (ghosts, Pac-Man).
 * 
 * @author Armin Reichert
 */
public abstract class AbstractMazeMover extends AbstractMazeResident implements MazeMover {

	private Direction moveDir = Direction.RIGHT;
	protected Direction nextDir;
	private Tile targetTile;
	protected List<Tile> targetPath;
	public boolean requireTargetPath;
	protected boolean enteredNewTile;
	protected int teleportTicks;
	protected int teleportTicksRemaining;

	@Override
	public void init() {
		moveDir = nextDir = RIGHT;
		targetTile = null;
		targetPath = Collections.emptyList();
		enteredNewTile = true;
		teleportTicksRemaining = -1;
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
	public Direction nextDir() {
		return nextDir;
	}

	@Override
	public void setNextDir(Direction dir) {
		nextDir = dir;
	}

	@Override
	public boolean enteredNewTile() {
		return enteredNewTile;
	}

	@Override
	public void setEnteredNewTile() {
		this.enteredNewTile = true;
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
	public List<Tile> targetPath() {
		return targetPath;
	}

	@Override
	public void setTargetPath(List<Tile> path) {
		path = Objects.requireNonNull(path, "Target path must not be null");
		targetPath = new ArrayList<>(path);
	}

	@Override
	public boolean requireTargetPath() {
		return requireTargetPath;
	}

	@Override
	public boolean canMoveForward() {
		return possibleSpeedTo(moveDir) > 0;
	}

	@Override
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile();
		return canMoveBetween(currentTile, maze().tileToDir(currentTile, dir));
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (neighbor.isWall()) {
			return false;
		}
		if (neighbor.isTunnel()) {
			return true; // includes tiles outside board used for teleportation!
		}
		return maze().insideBoard(neighbor);
	}

	@Override
	public Tile tilesAhead(int numTiles) {
		if (numTiles < 0) {
			throw new IllegalArgumentException("Number of tiles must be positive but is " + numTiles);
		}
		return maze().tileToDir(tile(), moveDir, numTiles);
	}

	@Override
	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		super.placeAtTile(tile, xOffset, yOffset);
		enteredNewTile = !tile.equals(tile());
	}

	public void setTeleportingDuration(int ticks) {
		teleportTicks = ticks;
	}

	/**
	 * Turns back to the reverse direction and triggers new steering.
	 */
	public void turnBack() {
		nextDir = moveDir = moveDir.opposite();
		enteredNewTile = true;
	}

	/**
	 * Moves or teleports the actor.
	 */
	protected void move() {
		boolean teleporting = teleport();
		if (!teleporting) {
			moveInsideMaze();
		}
	}

	/**
	 * When an actor (Ghost, Pac-Man) leaves a teleport tile towards the border, a timer is started and
	 * the actor is placed at the teleportation target and hidden (to avoid triggering events during
	 * teleportation). When the timer ends, the actor is made visible again.
	 * 
	 * @return <code>true</code> if teleportation is running
	 */
	private boolean teleport() {
		if (teleportTicksRemaining > 0) { // running
			teleportTicksRemaining -= 1;
			LOGGER.fine("Teleporting running, remaining:" + teleportTicksRemaining);
		}
		else if (teleportTicksRemaining == 0) { // completed
			teleportTicksRemaining = -1;
			show();
			LOGGER.fine("Teleporting complete");
		}
		else { // off
			int leftExit = (maze().tunnelExitLeft.col - 1) * Tile.SIZE;
			int rightExit = (maze().tunnelExitRight.col + 1) * Tile.SIZE;
			if (tf.getX() > rightExit) { // start
				teleportTicksRemaining = teleportTicks;
				tf.setX(leftExit);
				hide();
				LOGGER.fine("Teleporting started");
			}
			else if (tf.getX() < leftExit) { // start
				teleportTicksRemaining = teleportTicks;
				tf.setX(rightExit);
				hide();
				LOGGER.fine("Teleporting started");
			}
		}
		return teleportTicksRemaining != -1;
	}

	protected boolean snapToGrid() {
		return true;
	}

	/**
	 * Movement inside the maze. Handles changing the direction according to the intended move
	 * direction, moving around corners without losing alignment,
	 */
	private void moveInsideMaze() {
		Tile oldTile = tile();
		float speed = possibleSpeedTo(nextDir);
		if (speed > 0) {
			boolean turning = (nextDir == moveDir.turnLeft() || nextDir == moveDir.turnRight());
			if (turning && snapToGrid()) {
				tf.setPosition(oldTile.col * Tile.SIZE, oldTile.row * Tile.SIZE);
			}
			moveDir = nextDir;
		}
		else {
			speed = possibleSpeedTo(moveDir);
		}
		tf.setVelocity(Vector2f.smul(speed, Vector2f.of(moveDir.dx, moveDir.dy)));
		tf.move();
		enteredNewTile = !oldTile.equals(tile());
	}

	/**
	 * Computes how many pixels this entity can move towards the given direction without crossing the
	 * border to a forbidden neighbor tile.
	 */
	private float possibleSpeedTo(Direction dir) {
		if (canCrossBorderTo(dir)) {
			return maxSpeed();
		}
		switch (dir) {
		case UP:
			return -tile().row * Tile.SIZE + tf.getY();
		case RIGHT:
			return tile().col * Tile.SIZE - tf.getX();
		case DOWN:
			return tile().row * Tile.SIZE - tf.getY();
		case LEFT:
			return -tile().col * Tile.SIZE + tf.getX();
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}