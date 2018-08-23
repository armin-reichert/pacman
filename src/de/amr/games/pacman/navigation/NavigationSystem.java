package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Factory for navigation behaviors.
 * 
 * @author Armin Reichert
 */
public interface NavigationSystem {

	public static Navigation ambush(MazeMover victim) {
		return new FollowTargetTile(() -> aheadOf(victim, 4));
	}

	public static Navigation bounce() {
		return bouncer -> {
			MazeRoute route = new MazeRoute();
			route.dir = bouncer.isStuck() ? NESW.inv(bouncer.getCurrentDir()) : bouncer.getCurrentDir();
			return route;
		};
	}

	public static Navigation chase(MazeMover victim) {
		return new FollowTargetTile(victim::getTile);
	}

	public static Navigation flee(MazeMover chaser) {
		return new Flee(chaser);
	}

	public static Navigation followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static Navigation followPath(Tile target) {
		return new FollowPath(target);
	}

	public static Navigation followTargetTile(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile(targetTileSupplier);
	}

	public static Navigation forward() {
		return mover -> {
			MazeRoute route = new MazeRoute();
			route.dir = mover.getCurrentDir();
			return route;
		};
	}

	/**
	 * Inky's behaviour as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * <p>
	 * <cite> The blue ghost is nicknamed Inky, and remains inside the ghost house for a short time on
	 * the first level, not joining the chase until Pac-Man has managed to consume at least 30 of the
	 * dots. His English personality description is bashful, while in Japanese he is referred to as
	 * 気紛れ, kimagure, or “whimsical”.
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
	 */
	public static Navigation inkyChaseBehavior(Ghost blinky, PacMan pacMan) {
		return new FollowTargetTile(() -> {
			Maze maze = pacMan.getMaze();
			Tile blinkyPosition = blinky.getTile();
			Tile aheadPacMan = aheadOf(pacMan, 2);
			Tile target = new Tile(2 * aheadPacMan.col - blinkyPosition.col,
					2 * aheadPacMan.row - blinkyPosition.row);
			int row = Math.min(Math.max(0, target.row), maze.numRows() - 1);
			int col = Math.min(Math.max(0, target.col), maze.numCols() - 1);
			return new Tile(col, row);
		});
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
	 */
	public static Navigation clydeChaseBehavior(Ghost clyde, PacMan pacMan) {
		return new FollowTargetTile(() -> {
			double d = Vector2f.dist(clyde.getCenter(), pacMan.getCenter());
			return d >= 8 * Game.TS ? pacMan.getTile() : clyde.getMaze().getClydeScatteringTarget();

		});
	}

	public static Navigation scatter(Tile scatteringTarget) {
		return new FollowTargetTile(() -> scatteringTarget);
	}

	/**
	 * @param mover
	 *                a maze mover
	 * @param n
	 *                number of tiles
	 * @return the tile which lies <code>n</code> tiles ahead of the mover wrt its current move
	 *         direction. If this position is outside the maze, returns the tile <code>(n-1)</code>
	 *         tiles ahead an so on.
	 */
	static Tile aheadOf(MazeMover mover, int n) {
		Maze maze = mover.getMaze();
		Tile tile = mover.getTile();
		while (n >= 0) {
			Tile target = tile.tileTowards(mover.getCurrentDir(), n);
			if (maze.isValidTile(target)) {
				return target;
			}
			n -= 1;
		}
		return tile;
	}
}