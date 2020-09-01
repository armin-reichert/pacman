package de.amr.games.pacman.controller.creatures;

import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.MovementController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.TileWorldEntity;
import de.amr.statemachine.core.StateMachine;

/**
 * Guys can move through the world in a controlled way.
 * 
 * @author Armin Reichert
 */
public abstract class Guy<STATE> extends TileWorldEntity implements Lifecycle {

	public final String name;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	protected final MovementController movement;

	public Guy(World world, String name) {
		super(world);
		this.name = name;
		this.movement = new MovementController(this);
	}

	/**
	 * @return state machines controlling this entity
	 */
	public abstract Stream<StateMachine<?, ?>> machines();

	/**
	 * @return pixels this guy can move on the next tick.
	 */
	public abstract float getSpeed();

	/**
	 * Defines the steering for the given state.
	 * 
	 * @param state    current state of this guy
	 * @param steering steering for given state
	 */
	public abstract void setSteering(STATE state, Steering steering);

	/**
	 * @return current steering of this guy
	 */
	public abstract Steering getSteering();

	/**
	 * @param currentTile some tile
	 * @param neighbor    neighbor of tile
	 * @return if this guy can move from tile to neighbor
	 */
	public abstract boolean canMoveBetween(Tile currentTile, Tile neighbor);

	/**
	 * @param dir some direction
	 * @return if this guy can cross the border between its current tile and the neighbor to the given
	 *         direction
	 */
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public void placeAt(Tile tile, float dx, float dy) {
		Tile oldTile = tile();
		super.placeAt(tile, dx, dy);
		enteredNewTile = !tile().equals(oldTile);
	}

	/**
	 * Forces this guy to move to the given direction.
	 * 
	 * @param dir direction
	 */
	public void forceMoving(Direction dir) {
		wishDir = dir;
		movement.update();
	}

	/**
	 * Forces this guy to reverse its direction.
	 */
	public void reverseDirection() {
		forceMoving(moveDir.opposite());
	}

	/**
	 * Moves guy one step.
	 */
	public void makeStep() {
		final boolean aligned = getSteering().requiresGridAlignment();
		final float speed = getSpeed();
		final Tile tileBeforeMove = tile();

		// how far can we move?
		float possibleDistance = possibleMoveDistance(moveDir, speed);
		if (wishDir != null && wishDir != moveDir) {
			float possibleWishDirDistance = possibleMoveDistance(wishDir, speed);
			if (possibleWishDirDistance > 0) {
				if (wishDir == moveDir.left() || wishDir == moveDir.right()) {
					if (aligned) {
						placeAt(tileBeforeMove, 0, 0);
					}
				}
				moveDir = wishDir;
				possibleDistance = possibleWishDirDistance;
			}
		}
		tf.setVelocity(moveDir.vector().times(possibleDistance));
		tf.move();
		enteredNewTile = !tileBeforeMove.equals(tile());
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param dir a direction
	 * @return speed the creature's max. possible speed towards this direction
	 */
	private float possibleMoveDistance(Direction dir, float speed) {
		if (canCrossBorderTo(dir)) {
			return speed;
		}
		float availableX = tileOffsetX() - Tile.SIZE / 2;
		float availableY = tileOffsetY() - Tile.SIZE / 2;
		switch (dir) {
		case UP:
			return Math.min(availableY, speed);
		case DOWN:
			return Math.min(-availableY, speed);
		case LEFT:
			return Math.min(availableX, speed);
		case RIGHT:
			return Math.min(-availableX, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}