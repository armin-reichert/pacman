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
package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * A rectangular tiled area.
 * 
 * @author Armin Reichert
 */
public interface RectangularTiledArea extends TiledArea {

	/**
	 * @return width in number of tiles
	 */
	int width();

	/**
	 * @return height in number of tiles
	 */
	int height();

	/**
	 * @return column of left-upper tile
	 */
	int col();

	/**
	 * @return row of left-upper tile
	 */
	int row();

	default int numTiles() {
		return width() * height();
	}

	@Override
	default boolean includes(Tile tile) {
		return col() <= tile.col && tile.col < col() + width() && row() <= tile.row && tile.row < row() + height();
	}

	@Override
	default Stream<Tile> tiles() {
		return Stream.iterate(0, i -> i + 1).limit(width() * height())
				.map(i -> Tile.at(col() + i % width(), row() + i / width()));
	}

	default Vector2f center() {
		return Vector2f.of(((col() + 0.5f * width()) * Tile.SIZE), (row() + 0.5f * height()) * Tile.SIZE);
	}
}