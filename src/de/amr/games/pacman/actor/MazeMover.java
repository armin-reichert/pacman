package de.amr.games.pacman.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.games.pacman.model.Game.TS;
import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * An entity that knows how to move inside a tile-based maze.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends TileWorldEntity {

	private int currentDir;
	private int nextDir;

	public abstract Maze getMaze();

	public abstract boolean canTraverseDoor(Tile door);

	public abstract int supplyIntendedDir();

	public abstract float getSpeed();

	public int getCurrentDir() {
		return currentDir;
	}

	public void setCurrentDir(int dir) {
		this.currentDir = dir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int dir) {
		if (dir != nextDir) {
			nextDir = dir;
		}
	}

	public boolean isTurn(int currentDir, int nextDir) {
		return nextDir == NESW.left(currentDir) || nextDir == NESW.right(currentDir);
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
		return !canMove(currentDir);
	}

	public boolean canMove(int dir) {
		int col, row, newCol, newRow;
		Vector2f v = velocity(dir);
		switch (dir) {
		case Top4.E:
			col = Math.round(tf.getX() + getWidth() / 2) / TS;
			row = Math.round(tf.getY() + getHeight() / 2) / TS;
			newCol = Math.round(tf.getX() + getWidth()) / TS;
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.W:
			col = Math.round(tf.getX()) / TS;
			row = Math.round(tf.getY() + getHeight() / 2) / TS;
			newCol = Math.round(tf.getX() + v.x) / TS;
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.N:
			col = Math.round(tf.getX() + getWidth() / 2) / TS;
			row = Math.round(tf.getY() + getHeight() / 2) / TS;
			newRow = Math.round(tf.getY() + v.y) / TS;
			return newRow == row || canEnterTile(new Tile(col, newRow));
		case Top4.S:
			col = Math.round(tf.getX() + getWidth() / 2) / TS;
			row = Math.round(tf.getY() + getHeight() / 2) / TS;
			newRow = Math.round(tf.getY() + getHeight()) / TS;
			return newRow == row || canEnterTile(new Tile(col, newRow));
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	public void move() {
		if (canMove(getNextDir())) {
			if (isTurn(getCurrentDir(), getNextDir())) {
				align();
			}
			setCurrentDir(getNextDir());
		}
		if (!isStuck()) {
			tf.setVelocity(velocity(currentDir));
			tf.move();
			// check exit from teleport space
			if (tf.getX() > (getMaze().numCols() - 1 + getMaze().getTeleportLength()) * TS) {
				tf.setX(0);
			} else if (tf.getX() < -getMaze().getTeleportLength() * TS) {
				tf.setX((getMaze().numCols() - 1) * TS);
			}
		}
		int dir = supplyIntendedDir();
		if (dir != -1) {
			setNextDir(dir);
		}
	}

	private Vector2f velocity(int dir) {
		return smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir)));
	}
	
	/**
	 * @param n
	 *                number of tiles
	 * @return the tile which lies <code>n</code> tiles ahead of the mover wrt its current move
	 *         direction. If this position is outside the maze, returns the tile <code>(n-1)</code>
	 *         tiles ahead etc.
	 */
	public Tile ahead(int n) {
		Tile tile = getTile();
		while (n >= 0) {
			Tile ahead = tile.tileTowards(getCurrentDir(), n);
			if (getMaze().isValidTile(ahead)) {
				return ahead;
			}
			n -= 1;
		}
		return tile;
	}
}