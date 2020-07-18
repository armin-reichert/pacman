package de.amr.games.pacman.controller.steering.ghost;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.controller.steering.common.FollowingPath;
import de.amr.games.pacman.model.world.api.FoodContainer;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.WorldGraph;

/**
 * Lets a ghost escape to the "safest" maze corner depending on Pac-Man's current position. The
 * "safest" corner is defined by the maximum distance of Pac-Man to any tile on the path from the
 * actor's current position to the corner. When the target corner is reached the next corner is
 * computed.
 * 
 * @author Armin Reichert
 */
public class FleeingToSafeCorner extends FollowingPath {

	private final MobileLifeform refugee;
	private final MobileLifeform attacker;
	private final WorldGraph graph;
	private final Tile[] corners;
	private Tile safeCorner;

	public FleeingToSafeCorner(MobileLifeform refugee, MobileLifeform attacker) {
		super(refugee);
		this.refugee = refugee;
		this.attacker = attacker;
		World world = refugee.world();
		graph = new WorldGraph(world);
		corners = new Tile[] { world.capeNW(), world.capeNE(), world.capeSW(), world.capeSE() };
	}

	@Override
	public void steer() {
		if (path.size() == 0 || isComplete()) {
			safeCorner = computeSafeCorner();
			setPath(graph.shortestPath(refugee.tileLocation(), safeCorner));
		}
		super.steer();
	}

	@Override
	public boolean isComplete() {
		return refugee.tileLocation().equals(safeCorner);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	private Tile computeSafeCorner() {
		//@formatter:off
		return StreamUtils.permute(Arrays.stream(corners))
			.filter(corner -> !corner.equals(refugee.tileLocation()))
			.sorted(byDist().reversed())
			.findFirst().get();
		//@formatter:on
	}

	private Comparator<Tile> byDist() {
		return (corner1, corner2) -> {
			Tile refugeeLocation = refugee.tileLocation();
			Tile attackerLocation = attacker.tileLocation();
			double dist1 = minDistFromPath(attacker.world(), graph.shortestPath(refugeeLocation, corner1), attackerLocation);
			double dist2 = minDistFromPath(attacker.world(), graph.shortestPath(refugeeLocation, corner2), attackerLocation);
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