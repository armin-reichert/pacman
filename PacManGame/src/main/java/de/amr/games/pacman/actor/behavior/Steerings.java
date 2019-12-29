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
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Facade with different steerings.
 * 
 * @author Armin Reichert
 */
public interface Steerings {

	/**
	 * @param keys steering key codes in order UP, RIGHT, DOWN, LEFT
	 * 
	 * @return steering using the given keys
	 */
	static <T extends MazeMover> Steering<T> followsKeys(int... keys) {
		/*@formatter:off*/
		return actor -> Direction.dirs()
				.filter(dir -> Keyboard.keyDown(keys[dir.ordinal()]))
				.findAny()
				.ifPresent(actor::setNextDir);
		/*@formatter:on*/
	}

	/**
	 * Lets the ghost jump up and down.
	 * 
	 * @param maze            the maze
	 * @param ghostHousePlace the ghosthouse place number
	 * @return behavior which lets the ghost jump
	 */
	static Steering<Ghost> isJumpingUpAndDown(Maze maze, int ghostHousePlace) {
		return new JumpingUpAndDown(maze, ghostHousePlace);
	}

	/**
	 * Lets the actor move randomly though the maze while respecting the
	 * accessibility rules (for example, chasing and scattering ghost may not move
	 * upwards at dedicated tiles. Also reversing the direction is never allowed.
	 * 
	 * @return random move behavior
	 */
	static <T extends MazeMover> Steering<T> isMovingRandomlyWithoutTurningBack() {
		return new MovingRandomlyWithoutTurningBack<>();
	}

	/**
	 * Lets the actor head for a possibly changing target tile (may be unreachable)
	 * by taking the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> isHeadingFor(Supplier<Tile> fnTargetTile) {
		return new HeadingForTargetTile<>(fnTargetTile);
	}

	/**
	 * Lets the actor head for a fixed target tile (may be unreachable) by taking
	 * the "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> isHeadingFor(Tile targetTile) {
		return new HeadingForTargetTile<>(() -> targetTile);
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * 
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	static <T extends MazeMover> Steering<T> isFleeingToSafeCornerFrom(MazeMover attacker) {
		return new FleeingToSafeCorner<>(attacker.maze(), attacker::tile);
	}

	/**
	 * Lets the actor follow the shortest path to the target. This may be not
	 * possible, depending on the actor's current state.
	 * 
	 * @param maze     the maze
	 * @param fnTarget function supplying the target tile at time of decision
	 * 
	 * @return behavior where an actor follows the shortest (according to Manhattan
	 *         distance) path to a target tile
	 */
	static <T extends MazeMover> Steering<T> takingShortestPath(Maze maze, Supplier<Tile> fnTarget) {
		return new TakingShortestPath<>(maze, fnTarget);
	}

	/**
	 * Lets the actor follow a fixed path to the target.
	 * 
	 * @param maze the maze
	 * @param path the path to follow
	 * 
	 * @return behavior where actor follows the given path
	 */
	static <T extends MazeMover> Steering<T> takingFixedPath(Maze maze, List<Tile> path) {
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Path must not be empty");
		}
		return new TakingFixedPath<>(maze, path);
	}

	/**
	 * Lets a ghost enter the ghost house and move to its seat.
	 * 
	 * @param maze  the maze
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
	 * @param maze       the maze
	 * @param ghost      the ghost
	 * @param seatNumber seat number
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	static Steering<Ghost> isTakingSeat(Ghost ghost, int seatNumber) {
		return new EnteringGhostHouse(ghost, seatNumber);
	}

	/**
	 * Lets a ghost leave the ghost house.
	 * 
	 * @param maze the maze
	 * 
	 * @return behavior which lets a ghost leave the ghost house
	 */
	static Steering<Ghost> isLeavingGhostHouse(Maze maze) {
		return new LeavingGhostHouse(maze);
	}

	/**
	 * /** TODO: in progress.
	 */
	static Steering<PacMan> avoidingGhosts(Cast cast) {
		return new AvoidingGhosts(cast);
	}
}