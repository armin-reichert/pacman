package de.amr.games.pacman.actor.behavior;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Collections;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * Mix-in with ghost behaviors.
 * 
 * @author Armin Reichert
 */
public interface GhostBehaviors {

	/**
	 * Keeps the actor on its current tile.
	 * 
	 * @return behavior where ghost keeps its current position
	 */
	default Steering standingStill() {
		return ghost -> ghost.targetTile = ghost.currentTile();
	}

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior where ghost keeps its current move direction
	 */
	default Steering keepingDirection() {
		return ghost -> ghost.nextDir = ghost.moveDir;
	}

	/**
	 * Lets the ghost jump up and down.
	 * 
	 * @return bouncing behavior
	 */
	default Steering jumpingUpAndDown() {
		return ghost -> {
			if (ghost.moveDir == Top4.E || ghost.moveDir == Top4.W) {
				ghost.moveDir = Top4.N;
			}
			ghost.nextDir = ghost.isStuck() ? NESW.inv(ghost.moveDir) : ghost.moveDir;
		};
	}

	/**
	 * Heads for a target tile (may be unreachable) by taking the "best" direction at every
	 * intersection.
	 * 
	 * @param fnTarget
	 *                   function supplying the target tile
	 * @return behavior where ghost heads for the target tile
	 */
	default Steering headingFor(Supplier<Tile> fnTarget) {
		return new HeadingForTile(fnTarget);
	}

	/**
	 * Lets the ghost flee from the attacker by walking to a "safe" maze corner.
	 * 
	 * @param attacker
	 *                   the attacker (Pac-Man)
	 * @return behavior where ghost flees to a "safe" maze corner
	 */
	default Steering fleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner(attacker::currentTile);
	}

	/**
	 * <cite> Frightened mode is unique because the ghosts do not have a specific target tile while in
	 * this mode. Instead, they pseudo-randomly decide which turns to make at every intersection.
	 * </cite>
	 * 
	 * @return behavior where ghost flees from Pac-Man by taking random turns at each intersection
	 */
	default Steering fleeingRandomly() {
		return ghost -> {
			ghost.targetPath = Collections.emptyList();
			ghost.targetTile = null;
			if (ghost.enteredNewTile) {
				/*@formatter:off*/
				ghost.nextDir = permute(NESW.dirs())
					.filter(dir -> dir != NESW.inv(ghost.nextDir))
					.filter(dir -> ghost.canEnterTileTo(dir))
					.findAny().orElse(ghost.moveDir);
				/*@formatter:on*/
			}
		};
	}

	/**
	 * Lets the ghost follow a fixed path to the target.
	 * 
	 * @param fnTarget
	 *                   function supplying the target tile at time of decision
	 * @return behavior where ghost follows a fixed path
	 */
	default Steering followingFixedPath(Supplier<Tile> fnTarget) {
		return new FollowingFixedPath(fnTarget);
	}
}