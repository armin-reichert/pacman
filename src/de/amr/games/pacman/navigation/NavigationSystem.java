package de.amr.games.pacman.navigation;

import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for navigation behaviors. This is an alternative to a fat base class.
 * 
 * @author Armin Reichert
 */
public interface NavigationSystem<T extends MazeMover> {

	/**
	 * Ambushes the victim by targeting the tile which is the given number of tiles ahead of the
	 * victim position.
	 * 
	 * @param victim
	 *                 the attacked maze mover
	 * @param n
	 *                 the number of tiles ahead of the victim in its current direction. If this tile
	 *                 is outside of the maze, the tile <code>(n - 1)</code> ahead is used etc.
	 * @return ambush behavior
	 */
	public default Navigation<T> ambush(MazeMover victim, int n) {
		return headFor(() -> victim.ahead(n));
	}

	/**
	 * Clyde's chase behavior as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * 
	 * <P>
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
	 * @param attacker
	 *                   the attacker e.g. Clyde
	 * @param pacMan
	 *                   the attacked Pac-Man
	 * @param distance
	 *                   if the distance of the attacker to Pac-Man is less than this distance
	 *                   (measured in pixels), it rejects and moves to its scattering position.
	 *                   Otherwise it directly attacks PacMan.
	 */
	public default Navigation<T> attackAndReject(Ghost attacker, PacMan pacMan, int distance) {
		return headFor(
				() -> dist(attacker.getCenter(), pacMan.getCenter()) >= distance ? pacMan.getTile()
						: attacker.getScatteringTarget());
	}

	/**
	 * Attacks the victim directly by targeting the victim's current position.
	 * 
	 * @param victim
	 *                 the attacked maze mover
	 * @return direct attack behavior
	 */
	public default Navigation<T> attackDirectly(MazeMover victim) {
		return headFor(victim::getTile);
	}

	/**
	 * Inky's behaviour as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * <p>
	 * <cite> The blue ghost is nicknamed Inky, and remains inside the ghost house for a short time on
	 * the first level, not joining the chase until Pac-Man has managed to consume at least 30 of the
	 * dots. His English personality description is bashful, while in Japanese he is referred to as
	 * kimagure, or “whimsical”.
	 * </p>
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
	 * @param partner
	 *                  the maze mover which assists in computing the target tile (e.g. Blinky)
	 * @param pacMan
	 *                  the attacked Pac-Man
	 * @return partner attack behavior
	 */
	public default Navigation<T> attackWithPartner(Ghost partner, PacMan pacMan) {
		return headFor(() -> {
			Tile partnerTile = partner.getTile();
			Tile pacManTile = pacMan.ahead(2);
			Tile target = new Tile(2 * pacManTile.col - partnerTile.col,
					2 * pacManTile.row - partnerTile.row);
			// TODO: correctly project target tile to border
			Maze maze = pacMan.getMaze();
			int row = Math.min(Math.max(0, target.row), maze.numRows() - 1);
			int col = Math.min(Math.max(0, target.col), maze.numCols() - 1);
			return new Tile(col, row);
		});
	}

	/**
	 * Lets the maze mover bounce between walls.
	 * 
	 * @return bouncing behavior
	 */
	public default Navigation<T> bounce() {
		return bouncer -> new MazeRoute(
				bouncer.isStuck() ? NESW.inv(bouncer.getCurrentDir()) : bouncer.getCurrentDir());
	}

	/**
	 * Lets the maze mover flee from the given attacker.
	 * 
	 * @param attacker
	 *                   the attacker
	 * @return flight behavior
	 */
	public default Navigation<T> flee(MazeMover attacker) {
		return new EscapeIntoCorner<T>(attacker);
	}

	/**
	 * Lets the maze mover follow the keyboard input.
	 * 
	 * @param keyUp
	 *                   key code for upwards movement e.g. KeyEvent.VK_UP
	 * @param keyRight
	 *                   key code for right movement
	 * @param keyDown
	 *                   key code for down movement
	 * @param keyLeft
	 *                   key code for left movement
	 * @return keyboard steering behavior
	 */
	public default Navigation<T> followKeyboard(int keyUp, int keyRight, int keyDown, int keyLeft) {
		return mover -> {
			MazeRoute result = new MazeRoute();
			if (Keyboard.keyDown(keyUp)) {
				result.setDir(Top4.N);
			} else if (Keyboard.keyDown(keyRight)) {
				result.setDir(Top4.E);
			} else if (Keyboard.keyDown(keyDown)) {
				result.setDir(Top4.S);
			} else if (Keyboard.keyDown(keyLeft)) {
				result.setDir(Top4.W);
			} else {
				result.setDir(-1);
			}
			return result;
		};
	}

	/**
	 * Lets the maze mover follow the path to the given target. The path is computed on the graph of
	 * the maze and updated every time the move direction is queried. This can lead to lots of path
	 * finder calls.
	 * 
	 * @param target
	 *                 target tile (must be inside maze or teleport space)
	 * @return behavior following the path to the target
	 */
	public default Navigation<T> followDynamicRoute(Tile target) {
		return mover -> {
			MazeRoute route = new MazeRoute();
			route.setPath(mover.getMaze().findPath(mover.getTile(), target));
			route.setDir(mover.getMaze().alongPath(route.getPath()).orElse(-1));
			return route;
		};
	}

	/**
	 * Lets the maze mover follow a static path to the target. The static path is computed when the
	 * method {@link Navigation#computeStaticRoute(MazeMover)} is called.
	 * 
	 * @param target
	 *                 the target tile
	 * @return behavior following a static route
	 */
	public default Navigation<T> followStaticRoute(Tile target) {
		return new FollowFixedPath<T>(target);
	}

	/**
	 * Tries to reach the possibly unreachable target tile by chosing the best direction at every
	 * intersection.
	 * 
	 * @param targetTileSupplier
	 *                             function supplying the target tile at time of decision
	 * @return behavior head for the tile computed by the supplier
	 */
	public default Navigation<T> headFor(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile<T>(targetTileSupplier);
	}

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior keeping the current move direction
	 */
	public default Navigation<T> keepDirection() {
		return mover -> new MazeRoute(mover.getCurrentDir());
	}
}