package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.OptionalInt;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * Abstract base class for Pac-Man and the ghosts.
 * 
 * <p>
 * Implements movement inside the maze. Movement is controlled by supplying the intended move
 * direction before moving.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends SpriteEntity {

	/* The game model. */
	public final PacManGame game;

	/* Current move direction (Top4.N, Top4.E, Top4.S, Top4.W). */
	private int moveDir;

	/* The intended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	/* Tells if the last move entered a new tile position */
	private boolean enteredNewTile;

	protected MazeMover(PacManGame game) {
		this.game = game;
		moveDir = nextDir = Top4.E;
		enteredNewTile = false;
		// collision box size of maze movers is one tile, sprite size is larger!
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	public int getMoveDir() {
		return moveDir;
	}

	protected void setMoveDir(int moveDir) {
		this.moveDir = moveDir;
	}

	public int getNextDir() {
		return nextDir;
	}

	protected void setNextDir(int nextDir) {
		this.nextDir = nextDir;
	}

	public boolean enteredNewTile() {
		return enteredNewTile;
	}

	public Tile tilePosition() {
		Vector2f center = tf.getCenter();
		return game.maze.tileAt(round(center.x) / TS, round(center.y) / TS);
	}

	/**
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int numTiles) {
		return game.maze.tileToDir(tilePosition(), moveDir, numTiles);
	}

	/**
	 * Places this maze mover at the given tile, optionally with some pixel offset.
	 * 
	 * @param tile
	 *                  the tile where this maze mover is placed
	 * @param xOffset
	 *                  pixel offset in x-direction
	 * @param yOffset
	 *                  pixel offset in y-direction
	 */
	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		enteredNewTile = !tile.equals(tilePosition());
		tf.setPosition(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	/**
	 * Places this maze mover exactly over its current tile.
	 */
	public void align() {
		Tile tile = tilePosition();
		tf.setPosition(tile.col * TS, tile.row * TS);
	}

	/**
	 * @return <code>true</code> if this maze mover is exactly positioned over its current tile.
	 */
	public boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	/**
	 * @return the pixel offset in x-direction wrt the current tile
	 */
	public int getAlignmentX() {
		return round(tf.getX()) % TS;
	}

	/**
	 * @return the pixel offset in y-direction wrt the current tile
	 */
	public int getAlignmentY() {
		return round(tf.getY()) % TS;
	}

	/**
	 * @return a function returning the intended move direction
	 */
	public abstract OptionalInt supplyIntendedDir();

	/**
	 * @return the current speed (in pixels)
	 */
	public abstract float getSpeed();

	/**
	 * @param tile
	 *               some tile
	 * @return <code>true</code> if this maze mover can currently enter the tile
	 */
	public boolean canEnterTile(Tile tile) {
		if (game.maze.isWall(tile)) {
			return false;
		}
		if (game.maze.getTeleportRight().row == tile.row && game.maze.getTeleportRight().col + 1 == tile.col) {
			return true;
		}
		if (game.maze.getTeleportLeft().row == tile.row && game.maze.getTeleportLeft().col - 1 == tile.col) {
			return true;
		}
		return game.maze.insideBoard(tile);
	}

	/**
	 * @return <code>true</code> if the maze mover cannot move further towards its current direction
	 */
	public boolean isStuck() {
		return tf.getVelocity().length() == 0;
	}

	/**
	 * Moves this actor through the maze. Handles changing the direction according to the intended move
	 * direction, moving around corners without losing alignment, movement through the "teleport" tile
	 * and getting stuck.
	 */
	protected void move() {
		Tile oldTile = tilePosition();
		supplyIntendedDir().ifPresent(this::setNextDir);
		float speed = computeMaxSpeed(nextDir);
		if (speed > 0) {
			if (isTurning90Degrees()) {
				align();
			}
			setMoveDir(nextDir);
		}
		else {
			speed = computeMaxSpeed(moveDir);
		}
		tf.setVelocity(velocity(speed));
		tf.move();
		if (tf.getX() > (game.maze.numCols() - 1) * TS) {
			tf.setX(-TS);
		}
		else if (tf.getX() <= -TS) {
			tf.setX((game.maze.numCols() - 1) * TS);
		}
		enteredNewTile = oldTile != tilePosition();
	}

	private boolean isTurning90Degrees() {
		return nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir);
	}

	private Vector2f velocity(float speed) {
		return Vector2f.smul(speed, Vector2f.of(NESW.dx(moveDir), NESW.dy(moveDir)));
	}

	/*
	 * Computes how many pixels this entity can move towards the given direction in one frame.
	 */
	private float computeMaxSpeed(int dir) {
		final Tile currentTile = tilePosition();
		final Tile neighborTile = game.maze.tileToDir(currentTile, dir);
		final float speed = getSpeed();
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