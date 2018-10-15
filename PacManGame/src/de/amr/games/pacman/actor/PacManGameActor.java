package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;

/**
 * Base class for Pac-Man and the ghosts. An actor knows how to move in the maze and its movement
 * can be controlled by supplying the intended move direction at suitable points in time.
 * 
 * @author Armin Reichert
 */
public abstract class PacManGameActor extends SpriteEntity implements TilePlacedEntity {

	private final PacManGame game;

	/** Current move direction. See {@link Top4} for direction values. */
	private int currentDir;

	/** The indended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	protected PacManGameActor(PacManGame game) {
		this.game = game;
		currentDir = nextDir = Top4.E;
		// collision box size:
		tf.setWidth(getTileSize());
		tf.setHeight(getTileSize());
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
	 * @return intended direction
	 */
	public abstract int supplyIntendedDir();

	/**
	 * Returns the current move speed in pixels per tick.
	 * 
	 * @return move speed (pixels per tick)
	 */
	public abstract float getSpeed();

	public PacManGame getGame() {
		return game;
	}

	public Maze getMaze() {
		return game.getMaze();
	}

	public int getCurrentDir() {
		return currentDir;
	}

	public void setCurrentDir(int currentDir) {
		this.currentDir = currentDir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int nextDir) {
		this.nextDir = nextDir;
	}

	@Override
	public Transform tf() {
		return tf;
	}

	@Override
	public int getTileSize() {
		return PacManGame.TS;
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
		if (getMaze().inTeleportSpace(tile)) {
			return true;
		}
		if (!getMaze().isValidTile(tile)) {
			return false;
		}
		if (getMaze().isWall(tile)) {
			return false;
		}
		if (getMaze().isDoor(tile)) {
			return canTraverseDoor(tile);
		}
		return true;
	}

	public boolean isStuck() {
		if (inTeleportSpace()) {
			return false;
		}
		return possibleMove(getCurrentDir()).length() == 0;
	}

	/*
	 * Computes how far this actor can move towards the given direction.
	 */
	private Vector2f possibleMove(int dir) {
		final Tile currentTile = getTile();
		final Tile neighborTile = currentTile.tileTowards(dir);
		final Vector2f fullMove = Vector2f.smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir)));
		if (inTeleportSpace()) {
			return dir == Top4.E || dir == Top4.W ? fullMove : Vector2f.NULL;
		}
		if (canEnterTile(neighborTile)) {
			return fullMove;
		}
		switch (dir) {
		case Top4.E:
			float right = tf.getX() + tf.getWidth();
			return Vector2f.of(neighborTile.col * getTileSize() - right, 0);
		case Top4.W:
			float left = tf.getX();
			return Vector2f.of(currentTile.col * getTileSize() - left, 0);
		case Top4.N:
			float top = tf.getY();
			return Vector2f.of(0, currentTile.row * getTileSize() - top);
		case Top4.S:
			float bottom = tf.getY() + tf.getHeight();
			return Vector2f.of(0, neighborTile.row * getTileSize() - bottom);
		}
		throw new IllegalArgumentException("Illegal move direction: " + dir);
	}

	public void move() {
		// can we turn towards the intended direction?
		if (possibleMove(nextDir).length() > 0) {
			if (nextDir == NESW.left(currentDir) || nextDir == NESW.right(currentDir)) {
				align();
			}
			setCurrentDir(nextDir);
		}
		// move towards the current direction as far as possible
		Vector2f possibleMove = possibleMove(currentDir);
		if (possibleMove.length() > 0) {
			tf.setVelocity(possibleMove);
			tf.move();
			// check for exit from teleport space
			if (tf.getX() + tf.getWidth() < 0) {
				tf.setX(getMaze().numCols() * getTileSize());
			} else if (tf.getX() > (getMaze().numCols()) * getTileSize()) {
				tf.setX(-tf.getWidth());
			}
		}
		// query intended direction
		int dir = supplyIntendedDir();
		if (dir != -1) {
			setNextDir(dir);
		}
	}

	/**
	 * @param n
	 *            number of tiles
	 * @return the tile located <code>n</code> tiles ahead of the actor towards its current move
	 *         direction. If this position is outside the maze, returns the tile <code>(n-1)</code>
	 *         tiles ahead etc.
	 */
	public Tile ahead(int n) {
		final Tile tile = getTile();
		while (n >= 0) {
			Tile ahead = tile.tileTowards(currentDir, n);
			if (getMaze().isValidTile(ahead)) {
				return ahead;
			}
			n -= 1;
		}
		return tile;
	}
}