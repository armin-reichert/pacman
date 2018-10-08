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

	/** The current move direction. */
	private int currentDir;

	/** The indended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	public PacManGameActor() {
		currentDir = nextDir = Top4.E;
		// set collision box size
		tf.setWidth(getTileSize());
		tf.setHeight(getTileSize());
	}

	public abstract Maze getMaze();

	public abstract boolean canTraverseDoor(Tile door);

	public abstract int supplyIntendedDir();

	public abstract float getSpeed();

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
		return possibleMovement(getCurrentDir()).length() == 0;
	}

	/*
	 * Computes how far this actor can move towards the given direction. 
	 */
	private Vector2f possibleMovement(int dir) {
		final Tile currentTile = getTile();
		final Tile neighborTile = currentTile.tileTowards(dir);
		final Vector2f fullVelocity = Vector2f.smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir))); 
		if (inTeleportSpace()) {
			return dir == Top4.E || dir == Top4.W ? fullVelocity : Vector2f.NULL;
		}
		if (canEnterTile(neighborTile)) {
			return fullVelocity;
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
		// can we change the move direction?
		if (possibleMovement(nextDir).length() > 0) {
			if (isTurn(currentDir, nextDir)) {
				alignOverTile();
			}
			setCurrentDir(nextDir);
		}
		// move towards the current direction
		Vector2f possibleMovement = possibleMovement(currentDir);
		if (possibleMovement.length() > 0) {
			tf.setVelocity(possibleMovement);
			tf.move();
			// check exit from teleport space
			if (tf.getX() + tf.getWidth() < 0) {
				tf.setX(getMaze().numCols() * getTileSize());
			} else if (tf.getX() > (getMaze().numCols()) * getTileSize()) {
				tf.setX(-tf.getWidth());
			}
		}
		int dir = supplyIntendedDir();
		if (dir != -1) {
			setNextDir(dir);
		}
	}

	/**
	 * @param n
	 *            number of tiles
	 * @return the tile which lies <code>n</code> tiles ahead of the mover wrt its current move
	 *         direction. If this position is outside the maze, returns the tile <code>(n-1)</code>
	 *         tiles ahead etc.
	 */
	public Tile ahead(int n) {
		final Tile current = getTile();
		while (n >= 0) {
			Tile ahead = current.tileTowards(currentDir, n);
			if (getMaze().isValidTile(ahead)) {
				return ahead;
			}
			n -= 1;
		}
		return current;
	}
}