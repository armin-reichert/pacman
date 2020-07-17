package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.datastruct.StreamUtils.permute;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.common.TakingPrecomputedPath;
import de.amr.games.pacman.model.world.api.FoodContainer;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.WorldGraph;

/**
 * Lets a ghost escape to the "safest" maze corner depending on Pac-Man's current position. The
 * "safest" corner is defined by the maximum distance of Pac-Man to any tile on the path from the
 * actor's current position to the corner. When the target corner is reached the next corner is
 * computed.
 * 
 * @author Armin Reichert
 */
public class FleeingToSafeCorner extends TakingPrecomputedPath {

	public static FleeingToSafeCorner steer(Ghost refugee, MobileLifeform attacker) {
		return new FleeingToSafeCorner(refugee, attacker);
	}

	private final WorldGraph graph;
	private final Tile[] corners;

	private FleeingToSafeCorner(Ghost refugee, MobileLifeform attacker) {
		super(refugee, attacker::location);
		graph = new WorldGraph(world);
		corners = new Tile[] { world.capeNW(), world.capeNE(), world.capeSW(), world.capeSE() };
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	protected List<Tile> pathToTarget(MobileLifeform refugee, Tile targetTile) {
		Tile target = refugee.location();
		while (target.equals(refugee.location())) {
			target = safeCorner(refugee);
		}
		return graph.shortestPath(refugee.location(), target);
	}

	private Tile safeCorner(MobileLifeform refugee) {
		Tile refugeeTile = refugee.location();
		Tile chaserTile = fnTargetTile.get();
		//@formatter:off
		return permute(Arrays.stream(corners))
			.filter(corner -> !corner.equals(refugeeTile))
			.sorted(byDist(world,refugeeTile, chaserTile).reversed())
			.findFirst().get();
		//@formatter:on
	}

	private Comparator<Tile> byDist(FoodContainer world, Tile refugeeTile, Tile chaserTile) {
		return (corner1, corner2) -> {
			double dist1 = minDistFromPath(world, graph.shortestPath(refugeeTile, corner1), chaserTile);
			double dist2 = minDistFromPath(world, graph.shortestPath(refugeeTile, corner2), chaserTile);
			return Double.compare(dist1, dist2);
		};
	}

	private int manhattanDist(Tile t1, Tile t2) {
		// Note: tiles may be outside of board so we cannot use graph.manhattan()!
		int dx = t2.col - t1.col, dy = t2.row - t1.row;
		return Math.abs(dx) + Math.abs(dy);
	}

	private int minDistFromPath(FoodContainer world, List<Tile> path, Tile tile) {
		int min = Integer.MAX_VALUE;
		for (Tile t : path) {
			int dist = manhattanDist(t, tile);
			if (dist < min) {
				min = dist;
			}
		}
		return min;
	}
}