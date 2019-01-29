package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.OptionalInt;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

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

	/* Current move direction. See {@link Top4} for direction values. */
	private int moveDir;

	/* The intended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	protected MazeEntity() {
		moveDir = nextDir = Top4.E;
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

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

	private static int tileIndex(float coord) {
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

	public abstract OptionalInt supplyIntendedDir();

	public abstract float getSpeed();

	public abstract Maze getMaze();

	public abstract boolean canEnterTile(Tile tile);

	public boolean isStuck() {
		return tf.getVelocity().length() == 0;
	}

	public void move() {
		supplyIntendedDir().ifPresent(this::setNextDir);
		float speed = computeMaxSpeed(nextDir);
		if (speed > 0) {
			if (isTurning90()) {
				align();
			}
			setMoveDir(nextDir);
		} else {
			speed = computeMaxSpeed(moveDir);
		}
		tf.setVelocity(velocity(speed));
		if (speed > 0) {
			tf.move();
			// check for exit from teleport space
			if (tf.getX() + tf.getWidth() < 0) {
				tf.setX(getMaze().numCols() * TS);
			} else if (tf.getX() > (getMaze().numCols()) * TS) {
				tf.setX(-tf.getWidth());
			}
		}
	}

	private boolean isTurning90() {
		return nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir);
	}

	private Vector2f velocity(float speed) {
		return Vector2f.smul(speed, Vector2f.of(NESW.dx(moveDir), NESW.dy(moveDir)));
	}

	/*
	 * Computes how many pixels this entity can move towards the given direction in one frame.
	 */
	private float computeMaxSpeed(int dir) {
		final Tile currentTile = getTile();
		final Tile neighborTile = currentTile.tileTowards(dir);
		final float speed = getSpeed();
		if (getMaze().inTeleportSpace(currentTile)) {
			// in teleporting area only horizontal movement is possible
			return dir == Top4.N || dir == Top4.S ? 0 : speed;
		}
		if (canEnterTile(neighborTile)) {
			return speed;
		}
		float cappedSpeed = 0;
		switch (dir) {
		case Top4.E:
			cappedSpeed = neighborTile.col * TS - (tf.getX() + tf.getWidth());
			break;
		case Top4.W:
			cappedSpeed = tf.getX() - currentTile.col * TS;
			break;
		case Top4.N:
			cappedSpeed = tf.getY() - currentTile.row * TS;
			break;
		case Top4.S:
			cappedSpeed = neighborTile.row * TS - (tf.getY() + tf.getHeight());
			break;
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
		return Math.min(speed, cappedSpeed);
	}
}