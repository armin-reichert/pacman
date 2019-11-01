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
	private int moveDir;

	/* The intended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	/* Tells if the last move entered a new tile position */
	private boolean tileChanged;

	// Event publishing
	private final Set<Consumer<PacManGameEvent>> eventListeners;
	protected boolean eventsEnabled;

	protected MazeMover(PacManGame game, String name) {
		this.game = game;
		this.name = name;
		moveDir = nextDir = Top4.E;
		tileChanged = false;
		// collision box size of maze movers is one tile, sprite size is larger!
		tf.setWidth(TS);
		tf.setHeight(TS);
		// eventing
		eventListeners = new LinkedHashSet<>();
		eventsEnabled = true;
	}

	/**
	 * Computes the next move direction. This is usually different from the current move direction and
	 * will be taken as soon as possible, for example at the next intersection.
	 * 
	 * @return the next move direction to take
	 */
	public abstract OptionalInt computeNextDirection();

	/**
	 * @return the current speed (in pixels/tick)
	 */
	public abstract float getSpeed();

	public void addListener(Consumer<PacManGameEvent> listener) {
		eventListeners.add(listener);
	}

	public void removeListener(Consumer<PacManGameEvent> listener) {
		eventListeners.remove(listener);
	}

	public void publishEvent(PacManGameEvent event) {
		if (eventsEnabled) {
			LOGGER.info(String.format("%s publishing event '%s'", name, event));
			eventListeners.forEach(subscriber -> subscriber.accept(event));
		}
	}

	public void setEventsEnabled(boolean enabled) {
		this.eventsEnabled = enabled;
	}

	public boolean areEventsEnabled() {
		return eventsEnabled;
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

	public boolean hasEnteredNewTile() {
		return tileChanged;
	}

	/**
	 * @return the tile containing the center of this entity's collision box
	 */
	public Tile tilePosition() {
		Vector2f center = tf.getCenter();
		return game.maze.tileAt(round(center.x) / TS, round(center.y) / TS);
	}

	/**
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor towards his current
	 *         move direction.
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
		tileChanged = !tile.equals(tilePosition());
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
	 * Moves this actor through the maze. Handles changing the direction according to the intended
	 * move direction, moving around corners without losing alignment, "teleportation" and getting
	 * stuck.
	 */
	protected void move() {
		Tile prevTile = tilePosition();
		computeNextDirection().ifPresent(this::setNextDir);
		float speed = computeMaxSpeed(nextDir);
		if (speed > 0) {
			if (nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir)) {
				align();
			}
			moveDir = nextDir;
		}
		else {
			speed = computeMaxSpeed(moveDir);
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
		tileChanged = prevTile != tilePosition();
	}

	/*
	 * Computes how many pixels this entity can move towards the given direction in one frame.
	 */
	private float computeMaxSpeed(int dir) {
		final Tile currentTile = tilePosition();
		final Tile neighborTile = game.maze.tileToDir(currentTile, dir);
		final float fullSpeed = getSpeed();
		if (canEnterTile(neighborTile)) {
			return fullSpeed;
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
		return Math.min(fullSpeed, cappedSpeed);
	}
}