package de.amr.games.pacman.model.world.api;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface RectangularArea extends Area {

	/**
	 * @return width in number of tiles
	 */
	int width();

	/**
	 * @return height in number of tiles
	 */
	int height();

	/**
	 * @return column of left-upper corner
	 */
	int col();

	/**
	 * @return row of left-upper corner
	 */
	int row();

	@Override
	default boolean includes(Tile tile) {
		return col() <= tile.col && tile.col < col() + width() && row() <= tile.row && tile.row < row() + height();
	}

	@Override
	default Stream<Tile> tiles() {
		int minIndex = row() * width(), maxIndex = (row() + height() + 1) * width();
		return IntStream.range(minIndex, maxIndex).mapToObj(i -> Tile.at(i % width(), i / width()));
	}
}