package de.amr.games.pacman.actor.behavior;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.easy.game.math.Vector2f.euclideanDist;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.Ghost;
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
	 * @return the ghost implementing this mix-in
	 */
	Ghost theGhost();

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior where ghost keeps its current move direction
	 */
	default SteeringBehavior keepingDirection() {
		return ghost -> ghost.nextDir = ghost.moveDir;
	}

	/**
	 * Lets the ghost jump up and down.
	 * 
	 * @return bouncing behavior
	 */
	default SteeringBehavior jumpingUpAndDown() {
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
	default SteeringBehavior headingFor(Supplier<Tile> fnTarget) {
		return new HeadingFor(fnTarget);
	}

	/**
	 * Attacks a victim directly by targeting his current position.
	 * 
	 * @param victim
	 *                 the victim of the attack (e.g. Pac-Man)
	 * 
	 * @return behavior of attacking a victim directly
	 */
	default SteeringBehavior attackingDirectly(MazeMover victim) {
		return headingFor(victim::currentTile);
	}

	/**
	 * Ambushes a victim by heading for the tile located 4 tiles ahead of the victim's current position.
	 * 
	 * @param victim
	 *                 the ambushed victim (e.g. Pac-Man)
	 * @return ambushing behavior
	 */
	default SteeringBehavior ambushing(MazeMover victim) {
		return headingFor(() -> victim.tilesAhead(4));
	}

	/**
	 * Inky's attack behavior as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * 
	 * <p>
	 * <cite>Inky is difficult to predict, because he is the only one of the ghosts that uses a factor
	 * other than Pac-Man’s position/orientation when determining his target tile. Inky actually uses
	 * both Pac-Man’s position/facing as well as Blinky’s (the red ghost’s) position in his calculation.
	 * To locate Inky’s target, we first start by selecting the position two tiles in front of Pac-Man
	 * in his current direction of travel, similar to Pinky’s targeting method. From there, imagine
	 * drawing a vector from Blinky’s position to this tile, and then doubling the length of the vector.
	 * The tile that this new, extended vector ends on will be Inky’s actual target.</cite>
	 * </p>
	 * 
	 * @param partner
	 *                  the ghost which assists in attacking (Blinky)
	 * @param victim
	 *                  the attacked victim (Pac-Man)
	 * 
	 * @return behavior where victim is attacked with help of partner ghost
	 */
	default SteeringBehavior attackingWithPartner(MazeMover partner, MazeMover victim) {
		return headingFor(() -> {
			Tile partnerTile = partner.currentTile(), victimTile = victim.tilesAhead(2);
			return victim.maze.tileAt(2 * victimTile.col - partnerTile.col, 2 * victimTile.row - partnerTile.row);
		});
	}

	/**
	 * Clyde's chase behavior as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * 
	 * <p>
	 * <cite> The unique feature of Clyde’s targeting is that it has two separate modes which he
	 * constantly switches back and forth between, based on his proximity to Pac-Man. Whenever Clyde
	 * needs to determine his target tile, he first calculates his distance from Pac-Man. If he is
	 * farther than eight tiles away, his targeting is identical to Blinky’s, using Pac-Man’s current
	 * tile as his target. However, as soon as his distance to Pac-Man becomes less than eight tiles,
	 * Clyde’s target is set to the same tile as his fixed one in Scatter mode, just outside the
	 * bottom-left corner of the maze.</cite>
	 * </p>
	 * 
	 * <p>
	 * <cite> The combination of these two methods has the overall effect of Clyde alternating between
	 * coming directly towards Pac-Man, and then changing his mind and heading back to his corner
	 * whenever he gets too close. On the diagram above, the X marks on the path represent the points
	 * where Clyde’s mode switches. If Pac-Man somehow managed to remain stationary in that position,
	 * Clyde would indefinitely loop around that T-shaped area. As long as the player is not in the
	 * lower-left corner of the maze, Clyde can be avoided completely by simply ensuring that you do not
	 * block his “escape route” back to his corner. While Pac-Man is within eight tiles of the
	 * lower-left corner, Clyde’s path will end up in exactly the same loop as he would eventually
	 * maintain in Scatter mode. </cite>
	 * </p>
	 * 
	 * @param victim
	 *                        the victim (Pac-Man) getting attacked
	 * @param distance
	 *                        if the distance to the victim is less than this distance (measured in
	 *                        pixels), the attacker rejects and heads for its scattering position.
	 *                        Otherwise it directly attacks PacMan.
	 * @param scatterTarget
	 *                        tile ghost heads for in scattering mode
	 */
	default SteeringBehavior attackingCowardly(MazeMover victim, int distance, Tile scatterTarget) {
		return headingFor(() -> euclideanDist(theGhost().tf.getCenter(), victim.tf.getCenter()) > distance
				? victim.currentTile()
				: scatterTarget);
	}

	/**
	 * Lets the ghost flee from the attacker by walking to a "safe" maze corner.
	 * 
	 * @param attacker
	 *                   the attacker (Pac-Man)
	 * @return behavior where ghost flees to a "safe" maze corner
	 */
	default SteeringBehavior fleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner(attacker::currentTile);
	}

	/**
	 * <cite> Frightened mode is unique because the ghosts do not have a specific target tile while in
	 * this mode. Instead, they pseudo-randomly decide which turns to make at every intersection.
	 * </cite>
	 * 
	 * @return behavior where ghost flees Pac-Man by taking random turns at each intersection
	 */
	default SteeringBehavior fleeingRandomly() {
		return ghost -> {
			ghost.targetPath = Collections.emptyList();
			ghost.targetTile = null;
			ghost.nextDir = ghost.moveDir;
			if (ghost.maze.insideTunnel(ghost.currentTile())) {
				return;
			}
			if (ghost.enteredNewTile || ghost.isStuck()) {
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
	 * Lets the ghost dynamically follow the path to the given target. The path is computed on the graph
	 * of the maze and updated every time the move direction is queried. This can lead to lots of path
	 * finder calls!
	 * 
	 * @param fnTarget
	 *                   target tile supplier (this tile must be inside the maze or teleport space!)
	 * @return behavior following the path to the target
	 */
	default SteeringBehavior followingPathfinder(Supplier<Tile> fnTarget) {
		return ghost -> {
			ghost.nextDir = ghost.moveDir;
			if (ghost.enteredNewTile) {
				List<Tile> path = ghost.maze.findPath(ghost.currentTile(), fnTarget.get());
				ghost.targetPath = path;
				ghost.nextDir = ghost.maze.alongPath(path).orElse(ghost.moveDir);
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
	default SteeringBehavior followingFixedPath(Supplier<Tile> fnTarget) {
		return new FollowingFixedPath(fnTarget);
	}
}