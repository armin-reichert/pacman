package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.OptionalInt;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * An entity that can move through the maze. Movement is controlled by supplying the intended move
 * direction before moving.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends Entity {

	/** The maze where this entity moves in */
	public Maze maze;

	/** The current move direction (Top4.N, Top4.E, Top4.S, Top4.W). */
	public int moveDir;

	/** The intended move direction, actor takes this direction as soon as possible. */
	public int nextDir;

	/** Tells if the last move entered a new tile position */
	public boolean enteredNewTile;

	/* Timer for teleporting */
	private int teleportTicksRemaining;

	/* x-coordinate of teleport target */
	private int teleportTargetX;

	public MazeMover() {
		// set collision box size to one tile, sprite size may be larger
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	@Override
	public void init() {
		moveDir = nextDir = Top4.E;
		enteredNewTile = true;
		teleportTicksRemaining = Integer.MIN_VALUE;
		teleportTargetX = Integer.MIN_VALUE;
	}

	/**
	 * Returns the next move direction which usually differs from the current move direction and will be
	 * taken as soon as possible, for example at the next intersection.
	 * 
	 * @return the next move direction to take
	 */
	public abstract OptionalInt nextMoveDirection();

	/**
	 * @return the maximum possible speed (in pixels/tick) for the current frame. The actual speed can
	 *         be lower to avoid moving into inaccessible tiles.
	 */
	public abstract float maxSpeed();

	/**
	 * Moves this actor through the maze. Handles changing the direction according to the intended move
	 * direction, moving around corners without losing alignment, "teleportation" and getting stuck.
	 */
	protected void move() {
		nextMoveDirection().ifPresent(dir -> nextDir = dir);

		if (teleporting()) {
			return;
		}

		// normal movement
		Tile oldTile = currentTile();
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
		enteredNewTile = !oldTile.equals(currentTile());
	}

	/**
	 * Implements "teleporting". When an actor (Ghost, Pac-Man) leaves a teleport tile towards the
	 * border, a timer is started and the actor is hidden (to avoid triggering events during
	 * teleportation). When the timer ends, the actor is placed at the teleportation target and made
	 * visible again.
	 * 
	 * @return <code>true</code> if teleportation is running
	 */
	private boolean teleporting() {
		// check if teleporting is already running
		if (teleportTicksRemaining >= 1) {
			teleportTicksRemaining -= 1;
			return true;
		}

		// check if timer expired
		if (teleportTicksRemaining == 0) {
			LOGGER.fine("Teleporting ends");
			tf.setX(teleportTargetX);
			show();
			teleportTargetX = Integer.MIN_VALUE;
			teleportTicksRemaining = Integer.MIN_VALUE;
			return false;
		}

		// check if teleporting should be started
		int leftExit = (maze.teleportLeft.col - 1) * TS;
		int rightExit = (maze.teleportRight.col + 1) * TS;

		if (tf.getX() >= rightExit) {
			teleportTargetX = leftExit;
		}
		else if (tf.getX() <= leftExit) {
			teleportTargetX = rightExit;
		}

		// maybe start
		if (teleportTargetX != Integer.MIN_VALUE) {
			LOGGER.fine("Teleporting started");
			hide();
			teleportTicksRemaining = app().clock.sec(1f);
			return true;
		}

		return false;
	}

	/*
	 * Computes how many pixels this entity can actually move towards the given direction in the current
	 * frame without entering a forbidden tile.
	 */
	private float allowedSpeed(int dir) {
		if (canEnterTileTo(dir)) {
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
	 * @return the tile containing the center of this entity's collision box
	 */
	public Tile currentTile() {
		return maze.tileAt(col(), row());
	}

	/**
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int numTiles) {
		return maze.tileToDir(currentTile(), moveDir, numTiles);
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
		enteredNewTile = !tile.equals(currentTile());
		tf.setPosition(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	/**
	 * Common logic for Pac-Man and ghosts: walls can never be entered, teleportation is possible.
	 * 
	 * @param tile
	 *               some tile, may also be outside of the board
	 * @return <code>true</code> if this maze mover can enter the given tile
	 */
	public boolean canEnterTile(Tile tile) {
		if (maze.isWall(tile)) {
			return false;
		}
		if (maze.insideTunnel(tile)) {
			return true; // includes tiles outside board used for teleportation!
		}
		return maze.insideBoard(tile);
	}

	/**
	 * @param dir
	 *              a direction (N, E, S, W)
	 * @return if the maze mover can enter the neighbor tile towards the given direction
	 */
	public boolean canEnterTileTo(int dir) {
		return canEnterTile(maze.tileToDir(currentTile(), dir));
	}

	/**
	 * @return <code>true</code> if the maze mover cannot move further towards its current direction
	 */
	public boolean isStuck() {
		return tf.getVelocity().length() == 0;
	}
}