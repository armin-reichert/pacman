package de.amr.games.pacman.actor.behavior;

import java.util.List;
import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.behavior.common.HeadingForTargetTile;
import de.amr.games.pacman.actor.behavior.common.MovingRandomlyWithoutTurningBack;
import de.amr.games.pacman.actor.behavior.common.TakingFixedPath;
import de.amr.games.pacman.actor.behavior.common.TakingShortestPath;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Interface with steerings that can be used by any maze mover.
 * 
 * @author Armin Reichert
 */
public interface SteerableMazeMover extends MazeMover {

	/**
	 * @param actor the steered actor
	 * @param keys  steering key codes in order UP, RIGHT, DOWN, LEFT
	 * 
	 * @return steering using the given keys
	 */
	default Steering isFollowingKeys(int up, int right, int down, int left) {
		/*@formatter:off*/
		int keys[] = { up, right, down, left};
		return () -> Direction.dirs()
				.filter(dir -> Keyboard.keyDown(keys[dir.ordinal()]))
				.findAny()
				.ifPresent(this::setWishDir);
		/*@formatter:on*/
	}

	/**
	 * Lets the actor move randomly though the maze while respecting the
	 * accessibility rules (for example, chasing and scattering ghost may not move
	 * upwards at dedicated tiles. Also reversing the direction is never allowed.
	 * 
	 * @return random move behavior
	 */
	default Steering isMovingRandomlyWithoutTurningBack() {
		return new MovingRandomlyWithoutTurningBack(this);
	}

	/**
	 * Lets the actor head for a variable (probably unreachable) target tile by
	 * taking the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	default Steering isHeadingFor(Supplier<Tile> fnTargetTile) {
		return new HeadingForTargetTile(this, fnTargetTile);
	}

	/**
	 * Lets the actor head for a constant (probably unreachable) target tile by
	 * taking the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	default Steering isHeadingFor(Tile targetTile) {
		return new HeadingForTargetTile(this, () -> targetTile);
	}

	/**
	 * Lets the actor follow the shortest path to the target. This may be not
	 * possible, depending on the actor's current state.
	 * 
	 * @param fnTarget function supplying the target tile at time of decision
	 * 
	 * @return behavior where an actor follows the shortest (according to Manhattan
	 *         distance) path to a target tile
	 */
	default Steering isTakingShortestPath(Supplier<Tile> fnTarget) {
		return new TakingShortestPath(this, fnTarget);
	}

	/**
	 * Lets the actor follow a fixed path to the target. As the rules for accessing
	 * tiles are not checked, the actor may get stuck.
	 * 
	 * @param path the path to follow
	 * 
	 * @return behavior where actor follows the given path
	 */
	default Steering takingFixedPath(List<Tile> path) {
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Path must not be empty");
		}
		return new TakingFixedPath(this, path);
	}

}
