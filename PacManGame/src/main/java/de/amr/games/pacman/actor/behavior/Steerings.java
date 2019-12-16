package de.amr.games.pacman.actor.behavior;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.common.HeadingForTargetTile;
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
 * Steerings.
 * 
 * @author Armin Reichert
 */
public interface Steerings {

	/**
	 * @param keys steering key codes in order UP, RIGHT, DOWN, LEFT
	 * 
	 * @return steering using the given keys
	 */
	static <T extends MazeMover> Steering<T> steeredByKeys(int... keys) {
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
	 * @param baseTile  base tile for jump
	 * @param amplitude how far to jump from base tile up and down
	 * @return behavior which lets the ghost jump
	 */
	static Steering<Ghost> jumpingUpAndDown(Tile baseTile, int amplitude) {
		return new JumpingUpAndDown(baseTile, amplitude);
	}

	/**
	 * Lets the actor move randomly though the maze while respecting the
	 * accessibility rules (for example, chasing and scattering ghost may not move
	 * upwards at dedicated tiles. Also reversing the direction is never allowed.
	 * 
	 * @return random move behavior
	 */
	static <T extends MazeMover> Steering<T> movingRandomlyNoReversing() {
		/*@formatter:off*/
		return actor -> {
			actor.setTargetPath(Collections.emptyList());
			actor.setTargetTile(null);
			StreamUtils.permute(Direction.dirs())
				.filter(dir -> actor.enteredNewTile())
				.filter(dir -> dir != actor.moveDir().opposite())
				.filter(actor::canCrossBorderTo)
				.findFirst()
				.ifPresent(actor::setNextDir);
		};
		/*@formatter:on*/
	}

	/**
	 * Lets the actor head for a target tile (may be unreachable) by taking the
	 * "best" direction at every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> headingForTargetTile() {
		return new HeadingForTargetTile<>();
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * 
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	static <T extends MazeMover> Steering<T> fleeingToSafeCorner(MazeMover attacker) {
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
	 * Lets a ghost enter the ghost house and move to its target tile.
	 * 
	 * @param maze           the maze
	 * @param ghostHouseTile the ghost target tile in the house
	 * 
	 * @return behavior where a ghost enters the house and moves to its target tile
	 */
	static Steering<Ghost> enteringGhostHouse(Maze maze, Tile ghostHouseTile) {
		return new EnteringGhostHouse(maze, ghostHouseTile);
	}

	/**
	 * Lets a ghost leave the ghost house.
	 * 
	 * @param maze the maze
	 * 
	 * @return behavior where a ghost leaves the house
	 */
	static Steering<Ghost> leavingGhostHouse(Maze maze) {
		return new LeavingGhostHouse(maze);
	}

	/**
	 * /** TODO: in progress.
	 */
	static Steering<PacMan> avoidingGhosts() {
		return new AvoidingGhosts();
	}
}