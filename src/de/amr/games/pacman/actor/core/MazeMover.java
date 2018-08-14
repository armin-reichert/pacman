package de.amr.games.pacman.actor.core;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.NESW;
import static java.lang.Math.round;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * An entity that knows how to move over the tiles of the maze.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends TileWorldEntity {

	public final Maze maze;
	public final Tile homeTile;
	private int dir;
	private int nextDir;

	protected MazeMover(Maze maze, Tile homeTile) {
		this.maze = maze;
		this.homeTile = homeTile;
	}

	public abstract float getSpeed();

	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int dir) {
		this.nextDir = dir;
	}

	public int getIntendedNextDir() {
		return nextDir;
	}
	
	public boolean isInsideTunnel() {
		return maze.getContent(getTile()) == Content.TUNNEL;
	}

	public boolean isOutsideMaze() {
		Tile tile = getTile();
		return tile.row < 0 || tile.row >= maze.numRows() || tile.col < 0 || tile.col >= maze.numCols();
	}

	public void move() {
		if (isOutsideMaze()) {
			teleport();
			return;
		}
		nextDir = getIntendedNextDir();
		if (canMove(nextDir)) {
			dir = nextDir;
		}
		if (canMove(dir)) {
			tf.moveTo(computePosition(dir));
		} else {
			placeAt(getTile());
		}
	}

	private void teleport() {
		Tile tile = getTile();
		if (tile.col > (maze.numCols() - 1) + maze.getTeleportLength()) {
			// reenter maze from the left
			placeAt(0, tile.row);
		} else if (tile.col < -maze.getTeleportLength()) {
			// reenter maze from the right
			placeAt(maze.numCols() - 1, tile.row);
		} else {
			tf.moveTo(computePosition(dir));
		}
	}

	public boolean canMove(int targetDir) {
		if (isOutsideMaze()) {
			return true;
		}
		Tile current = getTile(), next = computeNextTile(current, targetDir);
		if (next.equals(current)) {
			return true;
		}
		if (maze.getContent(next) == WALL) {
			return false;
		}
		if (targetDir == NESW.right(dir) || targetDir == NESW.left(dir)) {
			placeAt(getTile()); // TODO this is not correct
			return isExactlyOverTile();
		}
		return true;
	}

	public Tile computeNextTile(Tile current, int dir) {
		Vector2f nextPosition = computePosition(dir);
		float x = nextPosition.x, y = nextPosition.y;
		switch (dir) {
		case Top4.W:
			return new Tile(round(x) / Game.TS, current.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / Game.TS, current.row);
		case Top4.N:
			return new Tile(current.col, round(y) / Game.TS);
		case Top4.S:
			return new Tile(current.col, round(y + getHeight()) / Game.TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private Vector2f computePosition(int dir) {
		Vector2f direction = Vector2f.of(NESW.dx(dir), NESW.dy(dir));
		Vector2f velocity = smul(getSpeed(), direction);
		return sum(tf.getPosition(), velocity);
	}
}