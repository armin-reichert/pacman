package de.amr.games.pacman.navigation;

import static de.amr.easy.game.math.Vector2f.euclideanDist;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.function.Supplier;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin with ghost behaviors.
 * 
 * @author Armin Reichert
 */
public interface GhostBehavior {

	/**
	 * @return the ghost implementing this mixin
	 */
	Ghost self();

	/**
	 * @param actor
	 *                   an actor
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>n</code> tiles ahead of the actor towards its current move
	 *         direction.
	 */
	static Tile ahead(MazeMover actor, int numTiles) {
		return actor.getTile().tileTowards(actor.getMoveDir(), numTiles);
	}

	/**
	 * Ambushes Pac-Man by heading for the tile ahead of Pac-Man's current position.
	 * 
	 * @param pacMan
	 *                   the ambushed Pac-Man
	 * @param numTiles
	 *                   the number of tiles ahead of Pac-Man in its current direction. If this tile
	 *                   is located outside of the maze, the tile <code>(n - 1)</code> ahead is used
	 *                   etc.
	 * @return ambushing behavior
	 */
	default Behavior<Ghost> ambush(PacMan pacMan, int numTiles) {
		return headFor(() -> ahead(pacMan, numTiles));
	}

	/**
	 * Attacks Pac-Man directly by targeting its current position.
	 * 
	 * @param pacMan
	 *                 the attacked Pac-Man
	 * 
	 * @return behavior of attacking Pac-Man directly
	 */
	default Behavior<Ghost> attackDirectly(PacMan pacMan) {
		return headFor(pacMan::getTile);
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
	 *                   the Pac-Man which gets attacked
	 * @param distance
	 *                   if the distance to Pac-Man is less than this distance (measured in pixels),
	 *                   the attacker rejects and heads for its scattering position. Otherwise it
	 *                   directly attacks PacMan.
	 */
	default Behavior<Ghost> attackOrReject(PacMan pacMan, int distance) {
		return headFor(() -> euclideanDist(self().tf.getCenter(), pacMan.tf.getCenter()) > distance
				? pacMan.getTile()
				: self().getScatteringTarget());
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
	default Behavior<Ghost> attackWith(Ghost blinky, PacMan pacMan) {
		return headFor(() -> {
			Tile b = blinky.getTile(), p = ahead(pacMan, 2);
			return new Tile(2 * p.col - b.col, 2 * p.row - b.row);
		});
	}

	/**
	 * Lets the ghost bounce between walls or other inaccessible tiles.
	 * 
	 * @return bouncing behavior
	 */
	default Behavior<Ghost> bounce() {
		return ghost -> new Route(ghost.isStuck() ? NESW.inv(ghost.getMoveDir()) : ghost.getMoveDir());
	}

	/**
	 * Lets the ghost flee from the attacker by heading to a safe maze corner.
	 * 
	 * @param attacker
	 *                   the attacker e.g. Pac-Man
	 * @return escaping behavior
	 */
	default Behavior<Ghost> flee(MazeMover attacker) {
		return new EscapeIntoCorner<>(attacker::getTile);
	}

	/**
	 * Lets the ghost dynamically follow the path to the given target. The path is computed on the
	 * graph of the maze and updated every time the move direction is queried. This can lead to lots
	 * of path finder calls!
	 * 
	 * @param targetTileSupplier
	 *                             target tile supplier (this tile must be inside the maze or teleport
	 *                             space!)
	 * @return behavior following the path to the target
	 */
	default Behavior<Ghost> followRoute(Supplier<Tile> targetTileSupplier) {
		return ghost -> {
			Route route = new Route();
			route.setPath(ghost.getMaze().findPath(ghost.getTile(), targetTileSupplier.get()));
			route.setDir(ghost.getMaze().alongPath(route.getPath()).orElse(-1));
			return route;
		};
	}

	/**
	 * Lets the ghost follow a fixed path to the target. The path is precomputed by calling
	 * {@link Behavior#computePath(MazeMover)}.
	 * 
	 * @param targetTileSupplier
	 *                             function supplying the target tile at time of decision
	 * @return behavior of following a fixed path
	 */
	default Behavior<Ghost> followFixedPath(Supplier<Tile> targetTileSupplier) {
		return new FollowFixedPath<>(targetTileSupplier);
	}

	/**
	 * Tries to reach a (possibly unreachable) target tile by chosing the best direction at every
	 * intersection.
	 * 
	 * @param targetTileSupplier
	 *                             function supplying the target tile at time of decision
	 * @return behavior heading for the target tile
	 */
	default Behavior<Ghost> headFor(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile<>(targetTileSupplier);
	}

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior keeping the current move direction
	 */
	default Behavior<Ghost> keepDirection() {
		return ghost -> new Route(ghost.getMoveDir());
	}
}