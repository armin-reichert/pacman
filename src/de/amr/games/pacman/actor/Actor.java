package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;
import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Controller;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.core.TilePositionedEntity;

/**
 * Mixin for actors in Pac-Man. Actors know how to move in the maze and can be controlled by
 * supplying the intended move direction at suitable points in time.
 * 
 * @author Armin Reichert
 */
public interface Actor extends Controller, TilePositionedEntity {

	int getCurrentDir();

	void setCurrentDir(int dir);

	int getNextDir();

	void setNextDir(int dir);

	Maze getMaze();

	boolean canTraverseDoor(Tile door);

	int supplyIntendedDir();

	float getSpeed();

	@Override
	default int getTileSize() {
		return Game.TS;
	}

	default boolean isTurn(int currentDir, int nextDir) {
		return nextDir == NESW.left(currentDir) || nextDir == NESW.right(currentDir);
	}

	default boolean inTeleportSpace() {
		return getMaze().inTeleportSpace(getTile());
	}

	default boolean inTunnel() {
		return getMaze().inTunnel(getTile());
	}

	default boolean inGhostHouse() {
		return getMaze().inGhostHouse(getTile());
	}

	default boolean canEnterTile(Tile tile) {
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

	default boolean isStuck() {
		return !canMove(getCurrentDir());
	}

	default boolean canMove(int dir) {
		int col, row, newCol, newRow;
		Transform tf = getTransform();
		Vector2f v = velocity(dir);
		switch (dir) {
		case Top4.E:
			col = tileCoord(tf.getX());
			row = tileCoord(tf.getY());
			newCol = round(tf.getX() + tf.getWidth()) / getTileSize();
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.W:
			col = round(tf.getX()) / getTileSize();
			row = tileCoord(tf.getY());
			newCol = round(tf.getX() + v.x) / getTileSize();
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.N:
			col = tileCoord(tf.getX());
			row = tileCoord(tf.getY());
			newRow = round(tf.getY() + v.y) / getTileSize();
			return newRow == row || canEnterTile(new Tile(col, newRow));
		case Top4.S:
			col = tileCoord(tf.getX());
			row = tileCoord(tf.getY());
			newRow = round(tf.getY() + tf.getHeight()) / getTileSize();
			return newRow == row || canEnterTile(new Tile(col, newRow));
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	default void move() {
		if (canMove(getNextDir())) {
			if (isTurn(getCurrentDir(), getNextDir())) {
				align();
			}
			setCurrentDir(getNextDir());
		}
		if (!isStuck()) {
			Transform tf = getTransform();
			tf.setVelocity(velocity(getCurrentDir()));
			tf.move();
			// check exit from teleport space
			if (tf.getX() > (getMaze().numCols() - 1 + getMaze().getTeleportLength()) * getTileSize()) {
				tf.setX(0);
			} else if (tf.getX() < -getMaze().getTeleportLength() * getTileSize()) {
				tf.setX((getMaze().numCols() - 1) * getTileSize());
			}
		}
		int dir = supplyIntendedDir();
		if (dir != -1) {
			setNextDir(dir);
		}
	}

	default Vector2f velocity(int dir) {
		return Vector2f.smul(getSpeed(), Vector2f.of(NESW.dx(dir), NESW.dy(dir)));
	}

	/**
	 * @param n
	 *            number of tiles
	 * @return the tile which lies <code>n</code> tiles ahead of the mover wrt its current move
	 *         direction. If this position is outside the maze, returns the tile <code>(n-1)</code>
	 *         tiles ahead etc.
	 */
	default Tile ahead(int n) {
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