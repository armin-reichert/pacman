package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.OptionalInt;

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
	private int moveDir;

	/** The indended move direction, actor turns to this direction as soon as possible. */
	private int nextDir;

	protected PacManGameActor(PacManGame game) {
		this.game = game;
		moveDir = nextDir = Top4.E;
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
	 * @return (optional) intended direction
	 */
	public abstract OptionalInt supplyIntendedDir();

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

	public int getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(int moveDir) {
		this.moveDir = moveDir;
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
		return possibleMoveDistance(getMoveDir()) == 0;
	}

	/**
	 * Computes how far this actor can move towards the given direction.
	 */
	private float possibleMoveDistance(int dir) {
		final float speed = getSpeed();
		if (inTeleportSpace()) {
			return dir == Top4.N || dir == Top4.S ? 0 : speed;
		}
		final Tile currentTile = getTile();
		final Tile neighborTile = currentTile.tileTowards(dir);
		if (canEnterTile(neighborTile)) {
			return speed;
		}
		switch (dir) {
		case Top4.E:
			float right = tf.getX() + tf.getWidth();
			return Math.min(speed, neighborTile.col * getTileSize() - right);
		case Top4.W:
			float left = tf.getX();
			return Math.min(speed, left - currentTile.col * getTileSize());
		case Top4.N:
			float top = tf.getY();
			return Math.min(speed, top - currentTile.row * getTileSize());
		case Top4.S:
			float bottom = tf.getY() + tf.getHeight();
			return Math.min(speed, neighborTile.row * getTileSize() - bottom);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	private Vector2f velocity(float speed, int dir) {
		return Vector2f.smul(speed, Vector2f.of(NESW.dx(dir), NESW.dy(dir)));
	}

	public void move() {
		// can we turn towards the intended direction?
		if (possibleMoveDistance(nextDir) > 0) {
			if (nextDir == NESW.left(moveDir) || nextDir == NESW.right(moveDir)) {
				align();
			}
			setMoveDir(nextDir);
		}
		// move towards the current direction as far as possible
		float possibleMoveDistance = possibleMoveDistance(moveDir);
		if (possibleMoveDistance > 0) {
			// LOGGER.info("Move " + possibleMoveDistance);
			tf.setVelocity(velocity(possibleMoveDistance, moveDir));
			tf.move();
			// check for exit from teleport space
			if (tf.getX() + tf.getWidth() < 0) {
				tf.setX(getMaze().numCols() * getTileSize());
			} else if (tf.getX() > (getMaze().numCols()) * getTileSize()) {
				tf.setX(-tf.getWidth());
			}
		}
		// query intended direction
		supplyIntendedDir().ifPresent(this::setNextDir);
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
			Tile ahead = tile.tileTowards(moveDir, n);
			if (getMaze().isValidTile(ahead)) {
				return ahead;
			}
			n -= 1;
		}
		return tile;
	}
}