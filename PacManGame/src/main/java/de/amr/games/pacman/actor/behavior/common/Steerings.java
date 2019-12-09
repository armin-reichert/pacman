package de.amr.games.pacman.actor.behavior.common;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Collections;
import java.util.function.Supplier;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.FleeingToSafeCorner;
import de.amr.games.pacman.actor.behavior.pacman.AvoidingGhosts;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Grid4Topology;

/**
 * Steerings.
 * 
 * @author Armin Reichert
 */
public interface Steerings {

	/**
	 * @param keys
	 *               steering key codes in order N, E, S, W
	 * @return steering using the given keys
	 */
	static <T extends MazeMover> Steering<T> steeredByKeys(int... keys) {
		/*@formatter:off*/
		return actor -> NESW.dirs()
				.filter(dir -> Keyboard.keyDown(keys[dir]))
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
			if (actor.moveDir == Grid4Topology.W || actor.moveDir == Grid4Topology.E) {
				actor.moveDir = Grid4Topology.S;
			}
			actor.targetTile = actor.tilesAhead(1);
			if (actor.isStuck()) {
				actor.nextDir = NESW.inv(actor.moveDir);
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
			actor.targetPath = Collections.emptyList();
			actor.targetTile = null;
			StreamUtils.permute(NESW.dirs())
				.filter(dir -> actor.enteredNewTile)
				.filter(dir -> dir != NESW.inv(actor.moveDir))
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
		return new FleeingToSafeCorner<>(attacker::tile);
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
	static <T extends MazeMover> Steering<T> followingShortestPath(Supplier<Tile> fnTarget) {
		return new TakingShortestPath<>(fnTarget);
	}

	/**
	 * TODO: in progress.
	 */
	static Steering<PacMan> avoidingGhosts() {
		return new AvoidingGhosts();
	}
}