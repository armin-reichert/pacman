package de.amr.games.pacman.actor.core;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Game.TS;
import static de.amr.games.pacman.model.Maze.NESW;
import static java.lang.Math.round;

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

	public abstract boolean canWalkThroughDoor(Tile door);

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
		this.nextDir = dir;
	}

	private boolean isTurn(int current, int next) {
		return next == NESW.left(current) || next == NESW.right(current);
	}

	public void move() {
		nextDir = supplyIntendedDir();
		if (canMove(nextDir)) {
			if (isTurn(currentDir, nextDir)) {
				align();
			}
			currentDir = nextDir;
		}
		if (inTeleportSpace()) {
			teleport();
		} else if (canMove(currentDir)) {
			tf.moveTo(positionAfterMove(currentDir));
		} else {
			align();
		}
	}

	public boolean canMove(int dir) {
		if (inTeleportSpace()) {
			// in teleport space direction can only be reversed
			return dir == currentDir || dir == NESW.inv(currentDir);
		}
		Tile next = computeTileAfterMove(dir);
		if (getMaze().getContent(next) == WALL) {
			return false;
		}
		if (getMaze().getContent(next) == DOOR) {
			return canWalkThroughDoor(next);
		}
		// turn left or right?
		if (isTurn(currentDir, dir)) {
			// TODO this is somewhat dubios but seems to work
			return dir == Top4.N || dir == Top4.S ? getAlignmentX() <= 1 : getAlignmentY() <= 1;
		}
		return true;
	}

	/**
	 * Computes the possibly adjusted tile position after a move in the given direction.
	 * 
	 * @param dir
	 *              move direction
	 * @return the tile after a move in that direction
	 */
	public Tile computeTileAfterMove(int dir) {
		Tile current = getTile();
		Vector2f pos = positionAfterMove(dir);
		switch (dir) {
		case Top4.W:
			return new Tile(round(pos.x) / TS, current.row);
		case Top4.E:
			return new Tile(round(pos.x + getWidth()) / TS, current.row);
		case Top4.N:
			return new Tile(current.col, round(pos.y) / TS);
		case Top4.S:
			return new Tile(current.col, round(pos.y + getHeight()) / TS);
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	public boolean inTeleportSpace() {
		return getMaze().isTeleportSpace(getTile());
	}

	/**
	 * "Teleport"
	 * 
	 * Leaves the maze on the left or right side, runs in "teleport space", reenters the maze on the
	 * opposite side.
	 */
	private void teleport() {
		int right = getMaze().numCols() - 1;
		int len = getMaze().getTeleportLength();
		if (tf.getX() > (right + len) * TS) {
			tf.setX(0);
		} else if (tf.getX() < -len * TS) {
			tf.setX(right * TS);
		} else {
			tf.moveTo(positionAfterMove(currentDir));
		}
	}

	private Vector2f positionAfterMove(int dir) {
		return sum(tf.getPosition(), smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir))));
	}
}