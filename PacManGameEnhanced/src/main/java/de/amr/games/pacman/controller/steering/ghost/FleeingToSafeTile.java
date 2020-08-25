package de.amr.games.pacman.controller.steering.ghost;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.SteeredMover;
import de.amr.games.pacman.controller.steering.common.FollowingPath;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MovingEntity;
import de.amr.games.pacman.model.world.graph.WorldGraph;
import de.amr.games.pacman.model.world.graph.WorldGraph.PathFinder;

/**
 * Lets a refugee escape to the "safest" of some dedicated maze tiles depending on the attackers'
 * current position. The "safest" corner is defined by the maximum distance of the attacker to any
 * tile on the path from the refugees' current position to the corner. When the target corner is
 * reached the next corner is computed.
 * 
 * @author Armin Reichert
 */
public class FleeingToSafeTile extends FollowingPath {

	private final MovingEntity attacker;
	private final World world;
	private final WorldGraph graph;
	private final List<Tile> capes;
	private final List<Tile> safeTiles;
	private Tile safeTile;

	public FleeingToSafeTile(Ghost refugee, MovingEntity attacker) {
		super(refugee);
		world = refugee.world;
		this.attacker = attacker;
		graph = new WorldGraph(world);
		graph.setPathFinder(PathFinder.BEST_FIRST_SEARCH);
		capes = world.capes();
		safeTiles = new ArrayList<>(capes);
	}

	@Override
	public void steer(SteeredMover guy) {
		if (path.size() == 0 || isComplete()) {
			safeTile = computeSafestCorner();
			setPath(graph.shortestPath(guy.tile(), safeTile));
		}
		super.steer(guy);
	}

	@Override
	public void init() {
		path.clear();
		safeTile = null;
	}

	@Override
	public boolean isComplete() {
		return guy.tile().equals(safeTile);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	private Tile computeSafestCorner() {
		return StreamUtils.permute(safeTiles.stream()).filter(tile -> tile != safeTile).sorted(byTileSafety()).findFirst()
				.get();
	}

	private Comparator<Tile> byTileSafety() {
		return (t1, t2) -> {
			Tile refugeeLocation = guy.tile();
			Tile attackerLocation = attacker.tile();
			double d1 = distanceFromPath(graph.shortestPath(refugeeLocation, t1), attackerLocation);
			double d2 = distanceFromPath(graph.shortestPath(refugeeLocation, t2), attackerLocation);
			return Double.compare(d2, d1); // larger distance comes first
		};
	}

	/*
	 * The distance of a tile from a path is the minimum of all distances between the tile and any path
	 * tile.
	 */
	private int distanceFromPath(List<Tile> path, Tile tile) {
		return path.stream().map(pathTile -> manhattanDist(pathTile, tile)).min(Integer::compare).orElse(Integer.MAX_VALUE);
	}

	private static int manhattanDist(Tile t1, Tile t2) {
		// Note: tiles may be outside of the world so we cannot use graph.manhattan()!
		int dx = t2.col - t1.col, dy = t2.row - t1.row;
		return Math.abs(dx) + Math.abs(dy);
	}
}