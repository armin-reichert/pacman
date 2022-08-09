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
package de.amr.games.pacmanfsm.model.world.components;

import de.amr.games.pacmanfsm.lib.Tile;

/**
 * A portal.
 * <p>
 * A horizontal portal connects a tile on the right edge of the world with a corresponding tile on
 * the left edge, a vertical portal connects a tile on the upper edge with a tile on the lower edge.
 * 
 * @author Armin Reichert
 */
public class Portal {

	/** left or top tile */
	public final Tile either;

	/** right or bottom tile */
	public final Tile other;

	/** If this is a horizontal or vertical portal */
	public final boolean vertical;

	public Portal(Tile either, Tile other, boolean vertical) {
		this.either = either;
		this.other = other;
		this.vertical = vertical;
	}

	public boolean includes(Tile tile) {
		return tile.equals(either) || tile.equals(other);
	}
}