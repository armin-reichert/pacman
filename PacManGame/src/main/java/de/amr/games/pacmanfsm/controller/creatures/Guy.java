/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.creatures;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.controller.StateMachineControlled;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.controller.steering.common.MovementController;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;
import de.amr.games.pacmanfsm.model.world.core.TileWorldEntity;

/**
 * Guys can move through the world in a controlled way.
 * 
 * @author Armin Reichert
 */
public abstract class Guy extends TileWorldEntity implements Lifecycle, StateMachineControlled {

	public final String name;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	protected final MovementController movement;

	protected Guy(TiledWorld world, String name) {
		super(world);
		this.name = name;
		this.movement = new MovementController(this);
	}

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
	public abstract void setSteering(Object state, Steering steering);

	/**
	 * @return current steering of this guy
	 */
	public abstract Steering getSteering();

	/**
	 * @param tile     some tile
	 * @param neighbor neighbor of tile
	 * @return if this guy can move from tile to neighbor
	 */
	public abstract boolean canMoveBetween(Tile tile, Tile neighbor);

	/**
	 * @param dir some direction
	 * @return if this guy can cross the border between its current tile and the neighbor to the given direction
	 */
	public boolean canMoveTo(Direction dir) {
		Tile currentTile = tile();
		Tile neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	public void move() {
		getSteering().steer(this);
		movement.update();
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
		final boolean gridAligned = getSteering().requiresGridAlignment();
		final float speed = getSpeed();
		final Tile tileBeforeMove = tile();
		float possibleDistance = possibleMoveDistance(moveDir, speed);
		if (wishDir != null && wishDir != moveDir) {
			float possibleWishDirDistance = possibleMoveDistance(wishDir, speed);
			if (possibleWishDirDistance > 0) {
				if (gridAligned && (wishDir == moveDir.left() || wishDir == moveDir.right())) {
					placeAt(tileBeforeMove, 0, 0);
				}
				moveDir = wishDir;
				possibleDistance = possibleWishDirDistance;
			}
		}
		tf.setVelocity(moveDir.vector().times(possibleDistance));
		tf.move();
		enteredNewTile = !tile().equals(tileBeforeMove);
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param dir a direction
	 * @return speed the creature's max. possible speed towards this direction
	 */
	private float possibleMoveDistance(Direction dir, float speed) {
		if (canMoveTo(dir)) {
			return speed;
		}
		float availableX = tileOffsetX() - Tile.TS / 2;
		float availableY = tileOffsetY() - Tile.TS / 2;
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