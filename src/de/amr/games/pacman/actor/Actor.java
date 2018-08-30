package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;
import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.core.TilePlacedEntity;

/**
 * Actors know how to move in the maze and can be controlled by supplying the intended move
 * direction at suitable points in time.
 * 
 * @author Armin Reichert
 */
public abstract class Actor extends GameEntityUsingSprites implements TilePlacedEntity {

	protected final Game game;
	private boolean visible;
	private int currentDir;
	private int nextDir;

	public Actor(Game game) {
		this.game = game;
		visible = true;
		currentDir = nextDir = Top4.E;
		tf.setWidth(getTileSize());
		tf.setHeight(getTileSize());
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
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

	public Maze getMaze() {
		return game.getMaze();
	}

	public abstract boolean canTraverseDoor(Tile door);

	public abstract int supplyIntendedDir();

	public abstract float getSpeed();

	@Override
	public Transform tf() {
		return tf;
	}

	@Override
	public int getTileSize() {
		return Game.TS;
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
		return !inTeleportSpace() && !canMove(getCurrentDir());
	}

	public boolean canMove(int dir) {
		int col, row, newCol, newRow;
		Vector2f v = velocity(dir);
		Vector2f center = tf.getCenter();
		switch (dir) {
		case Top4.E:
			col = tile(center.x);
			row = tile(center.y);
			newCol = round(tf.getX() + tf.getWidth()) / getTileSize();
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.W:
			col = round(tf.getX()) / getTileSize();
			row = tile(center.y);
			newCol = round(tf.getX() + v.x) / getTileSize();
			return newCol == col || canEnterTile(new Tile(newCol, row));
		case Top4.N:
			col = tile(center.x);
			row = tile(center.y);
			newRow = round(tf.getY() + v.y) / getTileSize();
			return newRow == row || canEnterTile(new Tile(col, newRow));
		case Top4.S:
			col = tile(center.x);
			row = tile(center.y);
			newRow = round(tf.getY() + tf.getHeight()) / getTileSize();
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
			tf.setVelocity(velocity(getCurrentDir()));
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

	@Override
	public void draw(Graphics2D g) {
		if (isVisible() && currentSprite() != null) {
			float dx = tf.getX() - (currentSprite().getWidth() - tf.getWidth()) / 2;
			float dy = tf.getY() - (currentSprite().getHeight() - tf.getHeight()) / 2;
			g.translate(dx, dy);
			currentSprite().draw(g);
			g.translate(-dx, -dy);
		}
	}
}