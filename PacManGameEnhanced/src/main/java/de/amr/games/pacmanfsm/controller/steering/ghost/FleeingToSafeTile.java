/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.steering.ghost;

import static de.amr.datastruct.StreamUtils.permute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.steering.common.FollowingPath;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.core.TileWorldEntity;
import de.amr.games.pacmanfsm.model.world.graph.WorldGraph;
import de.amr.games.pacmanfsm.model.world.graph.WorldGraph.PathFinder;

/**
 * Lets a refugee escape to the "safest" of some dedicated maze tiles depending on the attackers' current position. The
 * "safest" corner is defined by the maximum distance of the attacker to any tile on the path from the refugees' current
 * position to the corner. When the target corner is reached the next corner is computed.
 * 
 * @author Armin Reichert
 */
public class FleeingToSafeTile extends FollowingPath {

	private final TileWorldEntity attacker;
	private final WorldGraph graph;
	private final List<Tile> capes;
	private final List<Tile> safeTiles;
	private Tile safeTile;

	public FleeingToSafeTile(Ghost refugee, WorldGraph graph, TileWorldEntity attacker) {
		super(refugee);
		this.graph = graph;
		this.attacker = attacker;
		graph.setPathFinder(PathFinder.BEST_FIRST_SEARCH);
		capes = graph.world.capes();
		safeTiles = new ArrayList<>(capes);
	}

	@Override
	public void steer(Guy guy) {
		if (path.isEmpty() || isComplete()) {
			safeTile = computeSafestCorner();
			setPath(graph.findPath(guy.tile(), safeTile));
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
		return permute(safeTiles.stream()).filter(tile -> tile != safeTile).sorted(byTileSafety()).findFirst().orElse(null);
	}

	private Comparator<Tile> byTileSafety() {
		return (t1, t2) -> {
			Tile refugeeLocation = guy.tile();
			Tile attackerLocation = attacker.tile();
			double d1 = distanceFromPath(graph.findPath(refugeeLocation, t1), attackerLocation);
			double d2 = distanceFromPath(graph.findPath(refugeeLocation, t2), attackerLocation);
			return Double.compare(d2, d1); // larger distance comes first
		};
	}

	/*
	 * The distance of a tile from a path is the minimum of all distances between the tile and any path tile.
	 */
	private int distanceFromPath(List<Tile> path, Tile tile) {
		return path.stream().map(pathTile -> manhattanDist(pathTile, tile)).min(Integer::compare).orElse(Integer.MAX_VALUE);
	}

	private static int manhattanDist(Tile t1, Tile t2) {
		// Note: tiles may be outside of the world so we cannot use graph.manhattan()!
		int dx = t2.col - t1.col;
		int dy = t2.row - t1.row;
		return Math.abs(dx) + Math.abs(dy);
	}
}