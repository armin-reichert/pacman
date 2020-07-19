package de.amr.games.pacman.model.world.api;

import java.util.ArrayList;
import java.util.List;
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
		// TODO how to do this without collecting tiles?
		List<Tile> tiles = new ArrayList<>();
		for (int c = col(); c < col() + width(); ++c) {
			for (int r = row(); r < row() + height(); ++r) {
				tiles.add(Tile.at(c, r));
			}
		}
		return tiles.stream();
	}
}