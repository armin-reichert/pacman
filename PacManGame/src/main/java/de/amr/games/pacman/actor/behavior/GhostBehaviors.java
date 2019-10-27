package de.amr.games.pacman.actor.behavior;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.math.Vector2f.euclideanDist;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.function.Supplier;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
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
	Ghost self();

	/**
	 * Shortcut for access to maze.
	 * 
	 * @return the maze
	 */
	default Maze maze() {
		return self().game.maze;
	}

	/**
	 * Tries to reach a (possibly unreachable) target tile by choosing the "best" direction at every
	 * intersection.
	 * 
	 * @param fnTarget
	 *                   function supplying the target tile at time of decision
	 * @return behavior where ghost heads for the target tile
	 */
	default Behavior<Ghost> headingFor(Supplier<Tile> fnTarget) {
		return new HeadingFor<>(fnTarget);
	}

	/**
	 * Attacks Pac-Man directly by targeting his current position.
	 * 
	 * @param pacMan
	 *                 the attacked Pac-Man
	 * 
	 * @return behavior of attacking Pac-Man directly
	 */
	default Behavior<Ghost> attackingDirectly(PacMan pacMan) {
		return headingFor(pacMan::tilePosition);
	}

	/**
	 * Ambushes Pac-Man by heading for the tile located the given number of tiles ahead of Pac-Man's
	 * current position.
	 * 
	 * @param pacMan
	 *                 the ambushed Pac-Man
	 * @return ambushing behavior
	 */
	default Behavior<Ghost> ambushing(PacMan pacMan) {
		return headingFor(() -> pacMan.tilesAhead(4));
	}

	/**
	 * Inky's behaviour as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * 
	 * <p>
	 * <cite>Inky is difficult to predict, because he is the only one of the ghosts that uses a factor
	 * other than Pac-Man’s position/orientation when determining his target tile. Inky actually uses
	 * both Pac-Man’s position/facing as well as Blinky’s (the red ghost’s) position in his
	 * calculation. To locate Inky’s target, we first start by selecting the position two tiles in
	 * front of Pac-Man in his current direction of travel, similar to Pinky’s targeting method. From
	 * there, imagine drawing a vector from Blinky’s position to this tile, and then doubling the
	 * length of the vector. The tile that this new, extended vector ends on will be Inky’s actual
	 * target.</cite>
	 * </p>
	 * 
	 * @param blinky
	 *                 the ghost which assists in attacking (Blinky)
	 * @param pacMan
	 *                 the attacked Pac-Man
	 * 
	 * @return behavior where Pac-Man is attacked with help of partner ghost
	 */
	default Behavior<Ghost> attackingWithPartner(Ghost blinky, PacMan pacMan) {
		return headingFor(() -> {
			Tile b = blinky.tilePosition(), p = pacMan.tilesAhead(2);
			return maze().tileAt(2 * p.col - b.col, 2 * p.row - b.row);
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
	 * lower-left corner of the maze, Clyde can be avoided completely by simply ensuring that you do
	 * not block his “escape route” back to his corner. While Pac-Man is within eight tiles of the
	 * lower-left corner, Clyde’s path will end up in exactly the same loop as he would eventually
	 * maintain in Scatter mode. </cite>
	 * </p>
	 * 
	 * @param pacMan
	 *                        the Pac-Man which gets attacked
	 * @param distance
	 *                        if the distance to Pac-Man is less than this distance (measured in
	 *                        pixels), the attacker rejects and heads for its scattering position.
	 *                        Otherwise it directly attacks PacMan.
	 * @param scatterTarget
	 *                        tile ghost heads for in scattering mode
	 */
	default Behavior<Ghost> attackingAndRejecting(PacMan pacMan, int distance, Tile scatterTarget) {
		return headingFor(
				() -> euclideanDist(self().tf.getCenter(), pacMan.tf.getCenter()) > distance ? pacMan.tilePosition()
						: scatterTarget);
	}

	/**
	 * Lets the ghost bounce between walls or other inaccessible tiles.
	 * 
	 * @return bouncing behavior
	 */
	default Behavior<Ghost> bouncing() {
		return ghost -> new Route(ghost.isStuck() ? NESW.inv(ghost.getMoveDir()) : ghost.getMoveDir());
	}

	/**
	 * Lets the ghost flee from the attacker by walking to a "safe" maze corner.
	 * 
	 * @param attacker
	 *                   the attacker e.g. Pac-Man
	 * @return behavior where ghost flees to a "safe" maze corner
	 */
	default Behavior<Ghost> fleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner<>(maze(), attacker::tilePosition);
	}

	/**
	 * <cite> Frightened mode is unique because the ghosts do not have a specific target tile while in
	 * this mode. Instead, they pseudo-randomly decide which turns to make at every intersection.
	 * </cite>
	 * 
	 * @return behavior where ghost takes random turns at each intersection
	 */
	default Behavior<Ghost> fleeingRandomly() {
		return ghost -> {
			int currentDir = ghost.getMoveDir();
			Route route = new Route(currentDir);
			if (!ghost.hasEnteredNewTile() && !ghost.isStuck()) {
				// keep direction inside tile
				return route;
			}
			/*@formatter:off*/
			permute(NESW.dirs())
				.filter(dir -> dir != NESW.inv(currentDir))
				.filter(dir -> ghost.canEnterTile(maze().tileToDir(ghost.tilePosition(), dir)))
				.findFirst()
				.ifPresent(newDir -> {
					route.setDir(newDir);
					if (newDir != currentDir) {
						LOGGER.fine(String.format("Changing direction of '%s' from %s to %s", ghost.name,
							Top4.get().name(currentDir), Top4.get().name(newDir)));
					}
			});
			/*@formatter:on*/
			return route;
		};
	}

	/**
	 * Lets the ghost dynamically follow the path to the given target. The path is computed on the
	 * graph of the maze and updated every time the move direction is queried. This can lead to lots
	 * of path finder calls!
	 * 
	 * @param fnTarget
	 *                   target tile supplier (this tile must be inside the maze or teleport space!)
	 * @return behavior following the path to the target
	 */
	default Behavior<Ghost> followingPathfinder(Supplier<Tile> fnTarget) {
		return ghost -> {
			Route route = new Route();
			route.setPath(maze().findPath(ghost.tilePosition(), fnTarget.get()));
			route.setDir(maze().alongPath(route.getPath()).orElse(-1));
			return route;
		};
	}

	/**
	 * Lets the ghost follow a fixed path to the target. The path is precomputed by calling
	 * {@link Behavior#computePath(MazeMover)}.
	 * 
	 * @param fnTarget
	 *                   function supplying the target tile at time of decision
	 * @return behavior where ghost follows a fixed path
	 */
	default Behavior<Ghost> followingFixedPath(Supplier<Tile> fnTarget) {
		return new FollowingFixedPath<>(fnTarget);
	}

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior where ghost keeps its current move direction
	 */
	default Behavior<Ghost> keepingDirection() {
		return ghost -> new Route(ghost.getMoveDir());
	}
}