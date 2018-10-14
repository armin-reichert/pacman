package de.amr.games.pacman.navigation;

import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameActor;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Collection of actor navigation behaviors. Actors implementing this mixin-interface inherit all
 * behaviors.
 * 
 * @author Armin Reichert
 * 
 * @param <T>
 *          the actor type
 */
public interface ActorBehaviors<T extends PacManGameActor> {

	/**
	 * Ambushes the victim by heading for the tile the given number of tiles ahead of the victim's
	 * current position.
	 * 
	 * @param victim
	 *                        the ambushed actor
	 * @param numTilesAhead
	 *                        the number of tiles ahead of the victim in its current direction. If this
	 *                        tile is located outside of the maze, the tile <code>(n - 1)</code> ahead
	 *                        is used etc.
	 * @return ambush behavior
	 */
	default ActorBehavior<T> ambush(PacManGameActor victim, int numTilesAhead) {
		return headFor(() -> victim.ahead(numTilesAhead));
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
	 * @param attacker
	 *                   the attacker (Clyde)
	 * @param pacMan
	 *                   the attacked actor (Pac-Man)
	 * @param distance
	 *                   if the distance of the attacker to Pac-Man is less than this distance (measured
	 *                   in pixels), the attacker rejects and heads for its scattering position.
	 *                   Otherwise it directly attacks PacMan.
	 */
	default ActorBehavior<T> attackAndReject(Ghost attacker, PacMan pacMan, int distance) {
		return headFor(() -> dist(attacker.tf.getCenter(), pacMan.tf.getCenter()) >= distance ? pacMan.getTile()
				: attacker.getScatteringTarget());
	}

	/**
	 * Attacks the victim directly by targeting its current position.
	 * 
	 * @param victim
	 *                 the attacked actor
	 * 
	 * @return behavior of attacking the victim directly
	 */
	default ActorBehavior<T> attackDirectly(PacManGameActor victim) {
		return headFor(victim::getTile);
	}

	/**
	 * Inky's behaviour as described <a href=
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
	 * TODO: This code is much too complicated. It could be a lot easier if target tiles could be
	 * outside of the scope of valid tiles.
	 * 
	 * @param partnerGhost
	 *                       the ghost which assists in attacking (Blinky)
	 * @param pacMan
	 *                       the attacked Pac-Man
	 * 
	 * @return behavior where Pac-Man is attacked with help of partner ghost
	 */
	default ActorBehavior<T> attackWithPartnerGhost(Ghost partnerGhost, PacMan pacMan) {
		return headFor(() -> {
			Maze maze = partnerGhost.getMaze();
			int mazeWidth = maze.numCols() * TS;
			int mazeHeight = maze.numRows() * TS;
			Tile strut = pacMan.ahead(2);
			Vector2f partnerPosition = partnerGhost.tf.getCenter();
			Vector2f strutPosition = Vector2f.of(strut.col * TS + TS / 2, strut.row * TS + TS / 2);
			Vector2f targetPosition = doubledArrowTargetPosition(partnerPosition, strutPosition, mazeWidth,
					mazeHeight);
			// ensure target tile is inside maze
			int x = targetPosition.x < mazeWidth ? (int) targetPosition.x : mazeWidth - 1;
			int y = targetPosition.y < mazeHeight ? (int) targetPosition.y : mazeHeight - 1;
			return new Tile(x / TS, y / TS);
		});
	}

	/**
	 * Computes the position where the doubled arrow from the start position to the head position ends
	 * (inside the maze) or touches the maze bounds (if outside).
	 * 
	 * TODO: should be much simpler
	 * 
	 * @param arrowStart
	 *                     arrow start position (Blinky's position)
	 * @param arrowHead
	 *                     arrow head position (Pac-Man position + 2 tiles)
	 * @param width
	 *                     width of bounding box (maze width)
	 * @param height
	 *                     height of bounding box (maze height)
	 * 
	 * @return position where the doubled arrow ends, or projection point on maze border if doubled
	 *         arrow ends outside the maze
	 */
	static Vector2f doubledArrowTargetPosition(Vector2f arrowStart, Vector2f arrowHead, int width, int height) {

		final float dx = 2 * (arrowHead.x - arrowStart.x);
		final float dy = 2 * (arrowHead.y - arrowStart.y);
		final Vector2f target = Vector2f.of(arrowStart.x + dx, arrowStart.y + dy);

		// target position inside maze?
		if (0 <= target.x && target.x < width && 0 <= target.y && target.y < height) {
			// LOGGER.info(String.format("Target inside maze at (%.2f | %.2f)", t.x, t.y));
			return target;
		}

		// compute point where arrow leaves maze
		float lambda;
		float x, y;

		// 1. lower border?
		lambda = (height - arrowStart.y) / dy;
		if (lambda > 0 && Float.isFinite(lambda)) {
			x = arrowStart.x + lambda * dx;
			y = height;
			// LOGGER.info(String.format("Lower border touched at (%.2f | %.2f)", sx, sy));
			if (0 <= x && x < width) {
				return Vector2f.of(x, y);
			}
		}

		// 2. right border?
		lambda = (width - arrowStart.x) / dx;
		if (lambda > 0 && Float.isFinite(lambda)) {
			x = width;
			y = (arrowStart.y + lambda * dy);
			// LOGGER.info(String.format("Right border touched at (%.2f | %.2f)", sx, sy));
			if (0 <= y && y < height) {
				return Vector2f.of(x, y);
			}
		}

		// 3. upper border?
		lambda = -arrowStart.y / dy;
		if (lambda > 0 && Float.isFinite(lambda)) {
			x = arrowStart.x + lambda * dx;
			y = 0;
			// LOGGER.info(String.format("Upper border touched at (%.2f | %.2f)", sx, sy));
			if (0 <= x && x < width) {
				return Vector2f.of(x, y);
			}
		}

		// 4. left border?
		lambda = -arrowStart.x / dx;
		if (lambda > 0 && Float.isFinite(lambda)) {
			x = 0;
			y = arrowStart.y + lambda * dy;
			// LOGGER.info(String.format("Left border touched at (%.2f | %.2f)", sx, sy));
			if (0 <= y && y < height) {
				return Vector2f.of(x, y);
			}
		}

		return target;
	}

	/**
	 * Lets the actor bounce between walls or other inaccessible tiles.
	 * 
	 * @return bouncing behavior
	 */
	default ActorBehavior<T> bounce() {
		return bouncer -> new MazeRoute(
				bouncer.isStuck() ? NESW.inv(bouncer.getCurrentDir()) : bouncer.getCurrentDir());
	}

	/**
	 * Lets the maze mover flee from the given attacker by trying to escape to some safe maze corner.
	 * 
	 * @param attacker
	 *                   the attacker
	 * @return flight behavior
	 */
	default ActorBehavior<T> flee(PacManGameActor attacker) {
		return new EscapeIntoCorner<>(attacker::getTile);
	}

	/**
	 * Lets the actor follow the direction entered using the keyboard.
	 * 
	 * @param keyUp
	 *                   key code for upwards movement e.g. KeyEvent.VK_UP
	 * @param keyRight
	 *                   key code for right movement
	 * @param keyDown
	 *                   key code for down movement
	 * @param keyLeft
	 *                   key code for left movement
	 * 
	 * @return behavior following direction entered with the keyboard
	 */
	default ActorBehavior<T> followKeyboard(int keyUp, int keyRight, int keyDown, int keyLeft) {
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
	 * Lets the actor dynamically follow the path to the given target. The path is computed on the graph
	 * of the maze and updated every time the move direction is queried. This can lead to lots of path
	 * finder calls!
	 * 
	 * @param target
	 *                 target tile supplier (this tile must be inside the maze or teleport space!)
	 * @return behavior following the path to the target
	 */
	default ActorBehavior<T> followRoute(Supplier<Tile> targetSupplier) {
		return mover -> {
			MazeRoute route = new MazeRoute();
			route.setPath(mover.getMaze().findPath(mover.getTile(), targetSupplier.get()));
			route.setDir(mover.getMaze().alongPath(route.getPath()).orElse(-1));
			return route;
		};
	}

	/**
	 * Lets the actor follow a static route to the target. The path of that route is computed by calling
	 * the method {@link ActorBehavior#computePath(PacManGameActor)}.
	 * 
	 * @param targetTileSupplier
	 *                             function supplying the target tile at time of decision
	 * @return behavior following a static route
	 */
	default ActorBehavior<T> followFixedPath(Supplier<Tile> targetTileSupplier) {
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
	default ActorBehavior<T> headFor(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile<>(targetTileSupplier);
	}

	/**
	 * Keeps the current move direction.
	 * 
	 * @return behavior keeping the current move direction
	 */
	default ActorBehavior<T> keepDirection() {
		return mover -> new MazeRoute(mover.getCurrentDir());
	}
}