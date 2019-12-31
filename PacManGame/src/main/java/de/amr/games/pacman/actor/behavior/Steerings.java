package de.amr.games.pacman.actor.behavior;

import java.util.List;
import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.common.HeadingForTargetTile;
import de.amr.games.pacman.actor.behavior.common.MovingRandomlyWithoutTurningBack;
import de.amr.games.pacman.actor.behavior.common.TakingFixedPath;
import de.amr.games.pacman.actor.behavior.common.TakingShortestPath;
import de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse;
import de.amr.games.pacman.actor.behavior.ghost.FleeingToSafeCorner;
import de.amr.games.pacman.actor.behavior.ghost.JumpingUpAndDown;
import de.amr.games.pacman.actor.behavior.ghost.LeavingGhostHouse;
import de.amr.games.pacman.actor.behavior.pacman.AvoidingGhosts;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Factory of steerings.
 * 
 * @author Armin Reichert
 */
public interface Steerings {

	/**
	 * @param actor the steered actor
	 * @param keys  steering key codes in order UP, RIGHT, DOWN, LEFT
	 * 
	 * @return steering using the given keys
	 */
	static <T extends MazeMover> Steering<T> followsKeys(T actor, int up, int right, int down, int left) {
		/*@formatter:off*/
		int keys[] = { up, right, down, left};
		return () -> Direction.dirs()
				.filter(dir -> Keyboard.keyDown(keys[dir.ordinal()]))
				.findAny()
				.ifPresent(actor::setWishDir);
		/*@formatter:on*/
	}

	/**
	 * Lets the ghost jump up and down at its seat in the house.
	 * 
	 * @param ghost the jumping ghost
	 * @return behavior which lets the ghost jump
	 */
	static Steering<Ghost> isJumpingUpAndDown(Ghost ghost) {
		return new JumpingUpAndDown(ghost, ghost.seat());
	}

	/**
	 * Lets the actor move randomly though the maze while respecting the
	 * accessibility rules (for example, chasing and scattering ghost may not move
	 * upwards at dedicated tiles. Also reversing the direction is never allowed.
	 * 
	 * @return random move behavior
	 */
	static <T extends MazeMover> Steering<T> isMovingRandomlyWithoutTurningBack(T actor) {
		return new MovingRandomlyWithoutTurningBack<>(actor);
	}

	/**
	 * Lets the actor head for a variable (probably unreachable) target tile by
	 * taking the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> isHeadingFor(T actor, Supplier<Tile> fnTargetTile) {
		return new HeadingForTargetTile<>(actor, fnTargetTile);
	}

	/**
	 * Lets the actor head for a constant (probably unreachable) target tile by
	 * taking the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> isHeadingFor(T actor, Tile targetTile) {
		return new HeadingForTargetTile<>(actor, () -> targetTile);
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * 
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	static Steering<MazeMover> isFleeingToSafeCornerFrom(MazeMover attacker) {
		return new FleeingToSafeCorner(attacker, attacker::tile);
	}

	/**
	 * Lets the actor follow the shortest path to the target. This may be not
	 * possible, depending on the actor's current state.
	 * 
	 * @param actor    the steered actor
	 * @param fnTarget function supplying the target tile at time of decision
	 * 
	 * @return behavior where an actor follows the shortest (according to Manhattan
	 *         distance) path to a target tile
	 */
	static <T extends MazeMover> Steering<T> takingShortestPath(T actor, Supplier<Tile> fnTarget) {
		return new TakingShortestPath<>(actor, fnTarget);
	}

	/**
	 * Lets the actor follow a fixed path to the target. As the rules for accessing
	 * tiles are not checked, the actor may get stuck.
	 * 
	 * @param actor the steered actor
	 * @param path  the path to follow
	 * 
	 * @return behavior where actor follows the given path
	 */
	static <T extends MazeMover> Steering<T> takingFixedPath(T actor, List<Tile> path) {
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Path must not be empty");
		}
		return new TakingFixedPath<>(actor, path);
	}

	/**
	 * Lets a ghost enter the ghost house and move to its seat.
	 * 
	 * @param ghost the ghost
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	static Steering<Ghost> isTakingSeat(Ghost ghost) {
		return new EnteringGhostHouse(ghost, ghost.seat());
	}

	/**
	 * Lets a ghost enter the ghost house and move to the seat with the given
	 * number.
	 * 
	 * @param ghost the ghost
	 * @param seat  seat number
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	static Steering<Ghost> isTakingSeat(Ghost ghost, int seat) {
		return new EnteringGhostHouse(ghost, seat);
	}

	/**
	 * Lets a ghost leave the ghost house.
	 * 
	 * @param ghost the ghost
	 * 
	 * @return behavior which lets a ghost leave the ghost house
	 */
	static Steering<Ghost> isLeavingGhostHouse(Ghost ghost) {
		return new LeavingGhostHouse(ghost);
	}

	/**
	 * experimental.
	 */
	static Steering<PacMan> avoidingGhosts(Cast cast) {
		return new AvoidingGhosts(cast);
	}
}