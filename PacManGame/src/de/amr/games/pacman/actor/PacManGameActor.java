package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.easy.game.entity.GameEntityUsingSprites;
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
public abstract class PacManGameActor extends GameEntityUsingSprites implements TilePlacedEntity {

	/** The current move direction. */
	private int currentDir;

	/** The indended move direction which will be used as soon as turning becomes possible. */
	private int nextDir;

	public PacManGameActor() {
		currentDir = nextDir = Top4.E;
		// collision box size is one tile
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
		return !inTeleportSpace() && !canMove(getCurrentDir());
	}

	private boolean canMove(int dir) {
		int col, row, colNext, rowNext;
		Vector2f v = velocity(dir);
		Vector2f center = tf.getCenter();
		switch (dir) {
		case Top4.E:
			col = tileCoord(center.x);
			row = tileCoord(center.y);
			colNext = tileCoord(tf.getX() + tf.getWidth() /* + v.x */);
			return colNext == col || canEnterTile(new Tile(colNext, row));
		case Top4.W:
			col = tileCoord(tf.getX());
			row = tileCoord(center.y);
			colNext = tileCoord(tf.getX() + v.x);
			return colNext == col || canEnterTile(new Tile(colNext, row));
		case Top4.N:
			col = tileCoord(center.x);
			row = tileCoord(center.y);
			rowNext = tileCoord(tf.getY() + v.y);
			return rowNext == row || canEnterTile(new Tile(col, rowNext));
		case Top4.S:
			col = tileCoord(center.x);
			row = tileCoord(center.y);
			rowNext = tileCoord(tf.getY() + tf.getHeight() /* + v.y */);
			return rowNext == row || canEnterTile(new Tile(col, rowNext));
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	public void move() {
		if (canMove(nextDir)) {
			if (isTurn(currentDir, nextDir)) {
				alignOverTile();
			}
			setCurrentDir(nextDir);
		}
		if (!isStuck()) {
			tf.setVelocity(velocity(currentDir));
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

	public Vector2f velocity(int dir) {
		return Vector2f.smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir)));
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