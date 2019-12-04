package de.amr.games.pacman.actor.behavior.ghost;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Collections;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.common.HeadingForTargetTile;
import de.amr.games.pacman.actor.behavior.common.TakingShortestPath;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * Ghost steerings.
 * 
 * @author Armin Reichert
 */
public interface GhostSteerings {

	/**
	 * Lets the ghost jump up and down.
	 * 
	 * @return behavior which lets the ghost bounce vertically inside the currently
	 *         accessible area e.g. the ghost house
	 */
	static Steering<Ghost> jumpingUpAndDown() {
		return ghost -> {
			if (ghost.moveDir == Top4.E || ghost.moveDir == Top4.W) {
				ghost.moveDir = Top4.N;
			}
			ghost.nextDir = ghost.isStuck() ? NESW.inv(ghost.moveDir) : ghost.moveDir;
		};
	}

	/**
	 * Heads for a target tile (may be unreachable) by taking the "best" direction
	 * at every intersection.
	 * 
	 * @return behavior where ghost heads for the target tile
	 */
	static Steering<Ghost> headingForTargetTile() {
		return new HeadingForTargetTile<>();
	}

	/**
	 * Lets the ghost flee from the attacker by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacker (Pac-Man)
	 * @return behavior where ghost flees to a "safe" maze corner
	 */
	static Steering<Ghost> fleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner<>(attacker::tile);
	}

	/**
	 * Random movement inside the maze.
	 * 
	 * <p>
	 * <cite> Frightened mode is unique because the ghosts do not have a specific
	 * target tile while in this mode. Instead, they pseudo-randomly decide which
	 * turns to make at every intersection. </cite>
	 * 
	 * @return behavior where ghost takes random non-backwards turn at each
	 *         intersection respecting the rules which tiles are accessible for the
	 *         ghost in its current state
	 */
	static Steering<Ghost> movingRandomly() {
		return ghost -> {
			ghost.targetPath = Collections.emptyList();
			ghost.targetTile = null;
			if (ghost.enteredNewTile) {
				/*@formatter:off*/
				ghost.nextDir = permute(NESW.dirs())
					.filter(dir -> dir != NESW.inv(ghost.nextDir))
					.filter(ghost::canCrossBorderTo)
					.findAny().orElse(ghost.moveDir);
				/*@formatter:on*/
			}
		};
	}

	/**
	 * Lets the ghost follow the shortest path to the target. This may, depending on
	 * the ghost's current state, cause the ghost getting stuck because of the
	 * no-upwards-move-allowed crossings.
	 * 
	 * @param fnTarget function supplying the target tile at time of decision
	 * @return behavior where ghost follows the shortest (according to Manhattan
	 *         distance) path
	 */
	static Steering<Ghost> followingShortestPath(Supplier<Tile> fnTarget) {
		return new TakingShortestPath<>(fnTarget);
	}
}