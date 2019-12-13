package de.amr.games.pacman.actor.behavior.common;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;

import java.util.Collections;
import java.util.function.Supplier;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.FleeingToSafeCorner;
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
	 * @param keys
	 *               steering key codes in order UP, RIGHT, DOWN, LEFT
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
	 * Lets the actor jump up and down.
	 * 
	 * @return behavior which lets the actor bounce vertically inside its currently accessible area e.g.
	 *         the ghost house or the current maze corridor
	 */
	static <T extends MazeMover> Steering<T> jumpingUpAndDown() {
		return actor -> {
			if (actor.moveDir() == LEFT || actor.moveDir() == RIGHT) {
				actor.setMoveDir(DOWN);
				if (!actor.canMoveForward()) {
					actor.setMoveDir(UP);
				}
			}
			actor.setTargetTile(actor.tilesAhead(1));
			if (!actor.canMoveForward()) {
				actor.setNextDir(actor.moveDir().opposite());
			}
		};
	}

	/**
	 * Lets the actor move randomly though the maze while respecting the accessibility rules (for
	 * example, chasing and scattering ghost may not move upwards at dedicated tiles. Also reversing the
	 * direction is never allowed.
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
	 * Lets the actor head for a target tile (may be unreachable) by taking the "best" direction at
	 * every intersection.
	 * 
	 * @return behavior where actor heads for the target tile
	 */
	static <T extends MazeMover> Steering<T> headingForTargetTile() {
		return new HeadingForTargetTile<>();
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker
	 *                   the attacking actor
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	static <T extends MazeMover> Steering<T> fleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner<>(attacker.maze(), attacker::tile);
	}

	/**
	 * Lets the actor follow the shortest path to the target. This may, depending on the actor's current
	 * state.
	 * 
	 * @param fnTarget
	 *                   function supplying the target tile at time of decision
	 * @return behavior where an actor follows the shortest (according to Manhattan distance) path to a
	 *         target tile
	 */
	static <T extends MazeMover> Steering<T> followingShortestPath(Maze maze, Supplier<Tile> fnTarget) {
		return new TakingShortestPath<>(maze, fnTarget);
	}

	/**
	 * TODO: in progress.
	 */
	static Steering<PacMan> avoidingGhosts() {
		return new AvoidingGhosts();
	}
}