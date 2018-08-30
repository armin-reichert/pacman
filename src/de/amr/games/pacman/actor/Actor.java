package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Actors know how to move in the maze and can be controlled by supplying the intended move
 * direction at suitable points in time.
 * 
 * @author Armin Reichert
 */
public abstract class Actor extends GameEntityUsingSprites implements TilePlacement {

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
		if (canMove(getNextDir())) {
			if (isTurn(getCurrentDir(), getNextDir())) {
				alignOverTile();
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
		if (visible && currentSprite() != null) {
			Vector2f center = tf.getCenter();
			float dx = center.x - currentSprite().getWidth() / 2;
			float dy = center.y - currentSprite().getHeight() / 2;
			g.translate(dx, dy);
			currentSprite().draw(g);
			g.translate(-dx, -dy);
		}
	}
}