package de.amr.games.pacman.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.NESW;
import static java.lang.Math.round;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Knows about the rules of moving through the maze.
 * 
 * @author Armin Reichert
 */
public abstract class MazeMover extends TileWorldEntity {

	private static final int TELEPORT_TILES = 6;

	public final Maze maze;
	public final Tile homeTile;
	private int dir;
	private int nextDir;

	protected MazeMover(Maze maze, Tile homeTile) {
		this.maze = maze;
		this.homeTile = homeTile;
	}

	// Movement

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

	public int computeNextDir() {
		return nextDir;
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
		nextDir = computeNextDir();
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
		if (tile.col > (maze.numCols() - 1) + TELEPORT_TILES) {
			// reenter maze from the left
			placeAt(0, tile.row);
		} else if (tile.col < -TELEPORT_TILES) {
			// reenter maze from the right
			placeAt(maze.numCols() - 1, tile.row);
		} else {
			tf.moveTo(computePosition(dir));
		}
	}

	public boolean canMove(int goal) {
		if (isOutsideMaze()) {
			return true;
		}
		Tile current = getTile();
		if (goal == Top4.W && current.col <= 0) {
			return true; // enter teleport space on the left
		}
		if (goal == Top4.E && current.col >= maze.numCols() - 1) {
			return true; // enter teleport space on the right
		}
		Tile next = computeNextTile(current, goal);
		if (next.equals(current)) {
			return true; // move doesn't leave current tile
		}
		if (maze.getContent(next) == WALL) {
			return false;
		}
		if (goal == NESW.right(dir) || goal == NESW.left(dir)) {
			placeAt(getTile()); // TODO this is not 100% correct
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
		Vector2f v_dir = Vector2f.of(NESW.dx(dir), NESW.dy(dir));
		return sum(tf.getPosition(), smul(getSpeed(), v_dir));
	}
}