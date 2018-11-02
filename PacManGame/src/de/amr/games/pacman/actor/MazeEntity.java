package de.amr.games.pacman.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.OptionalInt;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Base class for Pac-Man and the ghosts.
 * 
 * <p>
 * Implements movement inside the maze. Movement is controlled by supplying the intended move
 * direction before moving.
 * 
 * @author Armin Reichert
 */
public abstract class MazeEntity extends SpriteEntity {

	/** Current move direction. See {@link Top4} for direction values. */
	private int moveDir;

	/** The intended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	protected MazeEntity() {
		moveDir = nextDir = Top4.E;
		// collision box size:
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	public int tileIndex(float coord) {
		return round(coord) / TS;
	}

	public Tile getTile() {
		Vector2f center = tf.getCenter();
		return new Tile(tileIndex(center.x), tileIndex(center.y));
	}

	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	public void align() {
		placeAtTile(getTile(), 0, 0);
	}

	public boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	public int getAlignmentX() {
		return round(tf.getX()) % TS;
	}

	public int getAlignmentY() {
		return round(tf.getY()) % TS;
	}

	/**
	 * Tells if this actor can traverse the door at the given tile position.
	 * 
	 * @param door
	 *               tile with door
	 * @return {@code true} if actor can traverse door in its current state
	 */
	public abstract boolean canTraverseDoor(Tile door);

	/**
	 * Supplies the intended move direction which will be taken as soon as possible.
	 * 
	 * @return (optional) intended direction
	 */
	public abstract OptionalInt supplyIntendedDir();

	/**
	 * Returns the current move speed in pixels per tick.
	 * 
	 * @return move speed (pixels per tick)
	 */
	public abstract float getSpeed();

	public abstract Maze getMaze();

	public int getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(int moveDir) {
		this.moveDir = moveDir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int nextDir) {
		this.nextDir = nextDir;
	}

	public boolean inTeleportSpace() {
		return getMaze().inTeleportSpace(getTile());
	}

	public boolean inTunnel() {
		return getMaze().inTunnel(getTile());
	}

	public boolean inGhostHouse() {
		return getMaze().inGhostHouse(getTile());
	}

	public boolean canEnterTile(Tile tile) {
		if (getMaze().isWall(tile)) {
			return false;
		}
		if (getMaze().isDoor(tile)) {
			return canTraverseDoor(tile);
		}
		return getMaze().inTeleportSpace(tile) || getMaze().isValidTile(tile);
	}

	public boolean isStuck() {
		return possibleMove(getMoveDir()) == 0;
	}

	/**
	 * Computes how far this actor can move towards the given direction.
	 */
	private float possibleMove(int dir) {
		final float speed = getSpeed();
		if (inTeleportSpace()) {
			return dir == Top4.N || dir == Top4.S ? 0 : speed;
		}
		final Tile currentTile = getTile();
		final Tile neighborTile = currentTile.tileTowards(dir);
		if (canEnterTile(neighborTile)) {
			return speed;
		}
		switch (dir) {
		case Top4.E:
			float right = tf.getX() + tf.getWidth();
			return Math.min(speed, neighborTile.col * TS - right);
		case Top4.W:
			float left = tf.getX();
			return Math.min(speed, left - currentTile.col * TS);
		case Top4.N:
			float top = tf.getY();
			return Math.min(speed, top - currentTile.row * TS);
		case Top4.S:
			float bottom = tf.getY() + tf.getHeight();
			return Math.min(speed, neighborTile.row * TS - bottom);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	public void move() {
		supplyIntendedDir().ifPresent(this::setNextDir);
		float possibleMove = possibleMove(nextDir);
		if (possibleMove > 0) {
			if (nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir)) {
				align();
			}
			setMoveDir(nextDir);
		}
		possibleMove = possibleMove(moveDir);
		if (possibleMove > 0) {
			Vector2f velocity = smul(possibleMove, Vector2f.of(NESW.dx(moveDir), NESW.dy(moveDir)));
			tf.setVelocity(velocity);
			tf.move();
			// check for exit from teleport space
			if (tf.getX() + tf.getWidth() < 0) {
				tf.setX(getMaze().numCols() * TS);
			} else if (tf.getX() > (getMaze().numCols()) * TS) {
				tf.setX(-tf.getWidth());
			}
		}
	}
}