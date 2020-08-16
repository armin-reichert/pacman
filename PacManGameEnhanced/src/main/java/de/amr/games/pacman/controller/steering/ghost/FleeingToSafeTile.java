package de.amr.games.pacman.controller.steering.ghost;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.common.FollowingPath;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.MovingGuy;
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

	private final MovingGuy attacker;
	private final World world;
	private final WorldGraph graph;
	private final List<Tile> capes;
	private final List<Tile> portalEntries;
	private final List<Tile> safeTiles;
	private Tile safeTile;
	private boolean passingPortal;

	public FleeingToSafeTile(Ghost refugee, MovingGuy attacker) {
		super(refugee.body);
		world = refugee.world;
		this.attacker = attacker;
		graph = new WorldGraph(world);
		graph.setPathFinder(PathFinder.BEST_FIRST_SEARCH);
		capes = world.capes();
		portalEntries = new ArrayList<Tile>();
		world.portals().forEach(portal -> {
			portalEntries.add(portal.either);
			portalEntries.add(portal.other);
		});
		safeTiles = new ArrayList<>(capes);
		safeTiles.addAll(portalEntries);
	}

	@Override
	public void steer(MovingGuy entity) {
		if (path.size() == 0 || isComplete()) {
			safeTile = computeSafestCorner();
			setPath(graph.shortestPath(entity.tile(), safeTile));
		}
		super.steer(entity);
	}

	@Override
	public void init() {
		path.clear();
		safeTile = null;
	}

	@Override
	public boolean isComplete() {
		if (passingPortal && !world.isTunnel(mover.tile())) {
			// refugee passed portal and tunnel, now compute new safe tile
			passingPortal = false;
			return true;
		}
		if (mover.tile().equals(safeTile)) {
			if (capes.contains(safeTile)) {
				return true;
			} else {
				// let refugee go through portal
				passingPortal = true;
			}
		}
		return false;
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
			Tile refugeeLocation = mover.tile();
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