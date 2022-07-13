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
package de.amr.games.pacman.controller.steering.common;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.graph.WorldGraph;

/**
 * Steers a guy following the shortest path (using graph path finding) to the target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends FollowingPath {

	private final WorldGraph graph;
	private final Supplier<Tile> fnTargetTile;

	public TakingShortestPath(Guy guy, WorldGraph graph, Supplier<Tile> fnTargetTile) {
		super(guy);
		this.graph = graph;
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(Guy guy) {
		if (path.size() == 0 || isComplete()) {
			setPath(graph.findPath(guy.tile(), fnTargetTile.get()));
		}
		super.steer(guy);
	}
}