package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.event.PacManGameEvent;
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
 * <p>
 * Maze movers can register event handlers for game events.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends SpriteEntity {

	/* The game model. */
	public final PacManGame game;

	/* My name */
	public final String name;

	/* Current move direction (Top4.N, Top4.E, Top4.S, Top4.W). */
	public int moveDir;

	/* The intended move direction, actor turns to this direction as soon as possible. */
	public int nextDir;

	/* Tells if the last move entered a new tile position */
	public boolean enteredNewTile;

	/* Event listeners. */
	private final Set<Consumer<PacManGameEvent>> gameEventListeners = new LinkedHashSet<>();

	protected MazeMover(PacManGame game, String name) {
		this.game = game;
		this.name = name;
		moveDir = nextDir = Top4.E;
		enteredNewTile = false;
		// collision box size is one tile, sprite size is larger!
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	/**
	 * Gets the next move direction which usually differs from the current move direction and will be
	 * taken as soon as possible, for example at the next intersection.
	 * 
	 * @return the next move direction to take
	 */
	public abstract OptionalInt getNextMoveDirection();

	/**
	 * @return the maximum possible speed (in pixels/tick) for the current frame. The actual speed can
	 *         be lower to avoid moving into inaccessible tiles.
	 */
	public abstract float maxSpeed();

	/**
	 * Adds a listener for the game events published by this maze mover.
	 * 
	 * @param listener
	 *                   event listener
	 */
	public void addGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.add(listener);
	}

	/**
	 * Removes the given game event listener.
	 * 
	 * @param listener
	 *                   event listener
	 */
	public void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.remove(listener);
	}

	/**
	 * Publishes the given event and informs all registered listeners.
	 * 
	 * @param event
	 *                a game event
	 */
	public void publishEvent(PacManGameEvent event) {
		LOGGER.info(String.format("%s reports '%s'", name, event));
		gameEventListeners.forEach(listener -> listener.accept(event));
	}

	/**
	 * @return the tile containing the center of this entity's collision box
	 */
	public Tile currentTile() {
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
		return game.maze.tileToDir(currentTile(), moveDir, numTiles);
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
	 * Places this maze mover exactly over its current tile.
	 */
	public void align() {
		Tile tile = currentTile();
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
	 * Common logic for Pac-Man and ghosts: walls can never be entered, teleportation is possible.
	 * 
	 * @param tile
	 *               some tile, may also be outside of the board
	 * @return <code>true</code> if this maze mover can enter the given tile
	 */
	public boolean canEnterTile(Tile tile) {
		if (game.maze.isWall(tile)) {
			return false;
		}
		if (tile.equals(game.maze.tileToDir(game.maze.getTeleportRight(), Top4.E))
				|| tile.equals(game.maze.tileToDir(game.maze.getTeleportLeft(), Top4.W))) {
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
	 * direction, moving around corners without losing alignment, "teleportation" and getting stuck.
	 */
	protected void move() {
		Tile prevTile = currentTile();
		getNextMoveDirection().ifPresent(dir -> nextDir = dir);
		float speed = computeActualSpeed(nextDir);
		if (speed > 0) {
			if (nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir)) {
				align();
			}
			moveDir = nextDir;
		}
		else {
			speed = computeActualSpeed(moveDir);
		}
		tf.setVelocity(Vector2f.smul(speed, Vector2f.of(NESW.dx(moveDir), NESW.dy(moveDir))));
		tf.move();
		int teleportLeftX = (game.maze.getTeleportLeft().col - 1) * TS;
		int teleportRightX = (game.maze.getTeleportRight().col + 1) * TS;
		if (tf.getX() >= teleportRightX) {
			tf.setX(teleportLeftX);
		}
		else if (tf.getX() <= teleportLeftX) {
			tf.setX(teleportRightX);
		}
		enteredNewTile = prevTile != currentTile();
	}

	/*
	 * Computes how many pixels this entity can actually move towards the given direction in the current
	 * frame.
	 */
	private float computeActualSpeed(int dir) {
		Tile currentTile = currentTile();
		Tile neighborTile = game.maze.tileToDir(currentTile, dir);
		if (canEnterTile(neighborTile)) {
			return maxSpeed();
		}
		switch (dir) {
		case Top4.E:
			return neighborTile.col * TS - (tf.getX() + tf.getWidth());
		case Top4.W:
			return tf.getX() - currentTile.col * TS;
		case Top4.N:
			return tf.getY() - currentTile.row * TS;
		case Top4.S:
			return neighborTile.row * TS - (tf.getY() + tf.getHeight());
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}