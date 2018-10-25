package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.PacManGameActor;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Attempt at implementing the original Ghost behavior as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>:
 *
 * <p>
 * <cite> The next step is understanding exactly how the ghosts attempt to reach their target tiles.
 * The ghosts’ AI is very simple and short-sighted, which makes the complex behavior of the ghosts
 * even more impressive. Ghosts only ever plan one step into the future as they move about the maze.
 * </cite>
 * </p>
 * 
 * <p>
 * <cite> Whenever a ghost enters a new tile, it looks ahead to the next tile that it will reach,
 * and makes a decision about which direction it will turn when it gets there. These decisions have
 * one very important restriction, which is that ghosts may never choose to reverse their direction
 * of travel. That is, a ghost cannot enter a tile from the left side and then decide to reverse
 * direction and move back to the left. The implication of this restriction is that whenever a ghost
 * enters a tile with only two exits, it will always continue in the same direction. </cite>
 * </p>
 * 
 * @author Armin Reichert
 */
public class FollowTargetTile<T extends PacManGameActor> implements ActorBehavior<T> {

	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public Route getRoute(T actor) {
		final Maze maze = actor.getMaze();
		final int actorDir = actor.getMoveDir();
		final Tile actorTile = actor.getTile();

		// ask for current target tile
		Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile must not be NULL");

		// if target tile is in teleport space, follow suitable tunnel entry
		if (maze.inTeleportSpace(targetTile)) {
			targetTile = targetTile.col < 0 ? maze.getLeftTunnelEntry() : maze.getRightTunnelEntry();
		}

		final Route route = new Route();
		route.setTarget(targetTile);

		// entering ghost house?
		if (maze.isGhostHouseEntry(actorTile) && maze.inGhostHouse(targetTile)) {
			route.setPath(maze.findPath(actorTile, targetTile));
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// also use path-finder inside ghost house
		if (maze.inGhostHouse(actorTile)) {
			if (maze.inGhostHouse(targetTile)) {
				// follow target inside ghost house
				route.setPath(maze.findPath(actorTile, targetTile));
			} else {
				// follow target outside of ghost house, go to Blinky's home tile to exit ghost house
				route.setPath(maze.findPath(actorTile, maze.getBlinkyHome()));
			}
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// if stuck, check if turning left or right is possible
		if (actor.isStuck()) {
			for (int turnDir : Arrays.asList(NESW.left(actorDir), NESW.right(actorDir))) {
				Tile turnTile = maze.neighborTile(actorTile, turnDir).get();
				if (actor.canEnterTile(turnTile)) {
					route.setDir(turnDir);
					return route;
				}
			}
		}

		// decide where to go if the next tile is an intersection
		final Tile nextTile = actorTile.tileTowards(actorDir);
		final boolean unrestricted = maze.inGhostHouse(nextTile) || maze.isUnrestrictedIntersection(nextTile);
		final boolean upForbidden = maze.isUpwardsBlockedIntersection(nextTile);
		if (unrestricted || upForbidden) {
			Stream<Integer> choices = NESW.dirs().boxed().filter(dir -> dir != NESW.inv(actorDir))
					.filter(dir -> unrestricted || dir != Top4.N);
			Optional<Integer> choice = findBestDir(actor, nextTile, targetTile, choices);
			if (choice.isPresent()) {
				route.setDir(choice.get());
				return route;
			}
		}

		// no direction could be determined
		route.setDir(-1);
		return route;
	}

	/**
	 * Find direction to neighbor tile with minimal distance to target.
	 */
	private Optional<Integer> findBestDir(PacManGameActor actor, Tile from, Tile target,
			Stream<Integer> choices) {
		final Maze maze = actor.getMaze();
		/*@formatter:off*/
		return choices
			.map(dir -> maze.neighborTile(from, dir))
			.filter(Optional::isPresent).map(Optional::get)
			.filter(actor::canEnterTile)
			.sorted(compareTilesByDistanceTo(target))
			.map(tile -> maze.direction(from, tile))
			.map(OptionalInt::getAsInt)
			.findFirst();
		/*@formatter:on*/
	}

	private static Comparator<Tile> compareTilesByDistanceTo(Tile target) {
		return (t1, t2) -> Float.compare(distance(t1, target), distance(t2, target));
	}

	private static float distance(Tile t1, Tile t2) {
		return Vector2f.dist(Vector2f.of(t1.col, t1.row), Vector2f.of(t2.col, t2.row));
	}
}