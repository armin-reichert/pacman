package de.amr.games.pacman.navigation;

import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for navigation behaviors. This is an alternative to a fat MazeMover base class.
 * 
 * @author Armin Reichert
 */
public interface NavigationSystem<T extends MazeMover> {

	public default Navigation<T> ambush(MazeMover victim) {
		return followTargetTile(() -> victim.ahead(4));
	}

	public default Navigation<T> attackDirectly(MazeMover victim) {
		return followTargetTile(victim::getTile);
	}

	public default Navigation<T> bounce() {
		return bouncer -> new MazeRoute(bouncer.isStuck() ? NESW.inv(bouncer.getCurrentDir()) : bouncer.getCurrentDir());
	}

	public default Navigation<T> flee(MazeMover chaser) {
		return new EscapeIntoCorner<T>(chaser);
	}

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

	public default Navigation<T> followPath(Tile target) {
		return mover -> {
			MazeRoute route = new MazeRoute();
			route.setPath(mover.getMaze().findPath(mover.getTile(), target));
			route.setDir(mover.getMaze().alongPath(route.getPath()).orElse(-1));
			return route;
		};
	}

	public default Navigation<T> followFixedPath(Tile target) {
		return new FollowFixedPath<T>(target);
	}

	public default Navigation<T> followTargetTile(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile<T>(targetTileSupplier);
	}

	public default Navigation<T> keepDirection() {
		return mover -> new MazeRoute(mover.getCurrentDir());
	}

	/**
	 * Inky's behaviour as described <a href=
	 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
	 * <p>
	 * <cite> The blue ghost is nicknamed Inky, and remains inside the ghost house for a short time on
	 * the first level, not joining the chase until Pac-Man has managed to consume at least 30 of the
	 * dots. His English personality description is bashful, while in Japanese he is referred to as 気紛れ,
	 * kimagure, or “whimsical”.
	 * </p>
	 * <p>
	 * <cite>Inky is difficult to predict, because he is the only one of the ghosts that uses a factor
	 * other than Pac-Man’s position/orientation when determining his target tile. Inky actually uses
	 * both Pac-Man’s position/facing as well as Blinky’s (the red ghost’s) position in his calculation.
	 * To locate Inky’s target, we first start by selecting the position two tiles in front of Pac-Man
	 * in his current direction of travel, similar to Pinky’s targeting method. From there, imagine
	 * drawing a vector from Blinky’s position to this tile, and then doubling the length of the vector.
	 * The tile that this new, extended vector ends on will be Inky’s actual target.</cite>
	 * </p>
	 */
	public default Navigation<T> chaseLikeInky(Ghost blinky, PacMan pacMan) {
		return followTargetTile(() -> {
			Tile b = blinky.getTile();
			Tile p = pacMan.ahead(2);
			Tile target = new Tile(2 * p.col - b.col, 2 * p.row - b.row);
			// TODO: correctly project target tile to border
			Maze maze = pacMan.getMaze();
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
	 * lower-left corner of the maze, Clyde can be avoided completely by simply ensuring that you do not
	 * block his “escape route” back to his corner. While Pac-Man is within eight tiles of the
	 * lower-left corner, Clyde’s path will end up in exactly the same loop as he would eventually
	 * maintain in Scatter mode. </cite>
	 * </p>
	 */
	public default Navigation<T> chaseLikeClyde(Ghost clyde, PacMan pacMan) {
		return followTargetTile(() -> dist(clyde.getCenter(), pacMan.getCenter()) >= 8 * Game.TS ? pacMan.getTile()
				: clyde.getScatteringTarget());
	}
}