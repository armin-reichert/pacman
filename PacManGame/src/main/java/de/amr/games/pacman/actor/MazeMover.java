package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.Collections;
import java.util.List;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * An entity following the rules for moving through the maze.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends Entity {

	public Maze maze;
	public int moveDir;
	public int nextDir;
	public Tile targetTile;
	public List<Tile> targetPath;
	public boolean enteredNewTile;
	private int teleportTicksRemaining;

	public MazeMover(Maze maze) {
		this.maze = maze;
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	@Override
	public void init() {
		moveDir = nextDir = Top4.E;
		targetTile = null;
		targetPath = Collections.emptyList();
		enteredNewTile = true;
		teleportTicksRemaining = -1;
	}

	public void setNextDir(int dir) {
		if (dir == Top4.N || dir == Top4.E || dir == Top4.S || dir == Top4.W) {
			nextDir = dir;
		}
		else {
			throw new IllegalArgumentException("Illegal direction value " + dir);
		}
	}

	/**
	 * Returns the squared straight line distance to the other actor measured in tile size.
	 * 
	 * @param other
	 *                other actor
	 * @return Euclidean distance (squared) in tile coordinates
	 */
	public int tileDistanceSq(MazeMover other) {
		return Tile.distanceSq(tile(), other.tile());
	}

	/**
	 * @return the maximum possible speed (in pixels/tick) for the current frame. The actual speed can
	 *         be lower to avoid moving into inaccessible tiles.
	 */
	public abstract float maxSpeed();

	/**
	 * Steers the actor by changing the intended move direction.
	 */
	protected abstract void steer();

	/**
	 * Moves or teleports the actor.
	 */
	protected void move() {
		boolean teleporting = teleport(app().clock.sec(1.0f));
		if (!teleporting) {
			moveInsideMaze();
		}
	}

	/**
	 * When an actor (Ghost, Pac-Man) leaves a teleport tile towards the border, a timer is started and
	 * the actor is placed at the teleportation target and hidden (to avoid triggering events during
	 * teleportation). When the timer ends, the actor is made visible again.
	 * 
	 * @param ticks
	 *                duration of teleportation in ticks
	 * @return <code>true</code> if teleportation is running
	 */
	private boolean teleport(int ticks) {
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
			int leftExit = (maze.tunnelLeftExit.col - 1) * TS;
			int rightExit = (maze.tunnelRightExit.col + 1) * TS;
			if (tf.getX() > rightExit) { // start
				teleportTicksRemaining = ticks;
				tf.setX(leftExit);
				hide();
				LOGGER.fine("Teleporting started");
			}
			else if (tf.getX() < leftExit) { // start
				teleportTicksRemaining = ticks;
				tf.setX(rightExit);
				hide();
				LOGGER.fine("Teleporting started");
			}
		}
		return teleportTicksRemaining != -1;
	}

	/**
	 * Movement inside the maze. Handles changing the direction according to the intended move
	 * direction, moving around corners without losing alignment,
	 */
	private void moveInsideMaze() {
		Tile oldTile = tile();
		float speed = allowedSpeed(nextDir);
		if (speed > 0) {
			if (nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir)) {
				tf.setPosition(oldTile.col * TS, oldTile.row * TS);
			}
			moveDir = nextDir;
		}
		else {
			speed = allowedSpeed(moveDir);
		}
		Vector2f direction = Vector2f.of(NESW.dx(moveDir), NESW.dy(moveDir));
		tf.setVelocity(Vector2f.smul(speed, direction));
		tf.move();
		enteredNewTile = !oldTile.equals(tile());
	}

	/*
	 * Computes how many pixels this entity can actually move towards the given direction in the current
	 * frame without entering a forbidden tile.
	 */
	private float allowedSpeed(int dir) {
		if (canCrossBorderTo(dir)) {
			return maxSpeed();
		}
		switch (dir) {
		case Top4.N:
			return -row() * TS + tf.getY();
		case Top4.E:
			return col() * TS - tf.getX();
		case Top4.S:
			return row() * TS - tf.getY();
		case Top4.W:
			return -col() * TS + tf.getX();
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	private int col() {
		return round(tf.getCenter().x) / TS;
	}

	private int row() {
		return round(tf.getCenter().y) / TS;
	}

	/**
	 * Returns the current tile of this actor which is the tile containing the center of the actor's
	 * collision box.
	 * 
	 * @return the current tile of this actor
	 */
	public Tile tile() {
		return maze.tileAt(col(), row());
	}

	/**
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int numTiles) {
		return maze.tileToDir(tile(), moveDir, numTiles);
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
		enteredNewTile = !tile.equals(tile());
		tf.setPosition(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	/**
	 * Tells if this actor can (currently) cross the border between the given tiles.
	 * 
	 * @param tile
	 *                   current tile
	 * @param neighbor
	 *                   neighbor tile, may also be a tile outside of the board
	 * @return <code>true</code> if this maze mover can cross the border between the given tiles
	 */
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isWall(neighbor)) {
			return false;
		}
		if (maze.isTunnel(neighbor)) {
			return true; // includes tiles outside board used for teleportation!
		}
		return maze.insideBoard(neighbor);
	}

	/**
	 * Tells if the neighbor tile towards the given direction can be entered from the current direction.
	 * 
	 * @param dir
	 *              a direction (N, E, S, W)
	 * @return if the maze mover can enter the neighbor tile towards the given direction
	 */
	public boolean canCrossBorderTo(int dir) {
		Tile currentTile = tile();
		return canMoveBetween(currentTile, maze.tileToDir(currentTile, dir));
	}

	/**
	 * @return <code>true</code> if the maze mover cannot move further towards its current direction
	 */
	public boolean isStuck() {
		return tf.getVelocity().length() == 0;
	}
}