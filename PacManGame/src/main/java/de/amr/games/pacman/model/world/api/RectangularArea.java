package de.amr.games.pacman.model.world.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

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
		List<Tile> tiles = new ArrayList<>();
		for (int col = col(); col < col() + width(); ++col) {
			for (int row = row(); row < row() + height(); ++row) {
				tiles.add(Tile.at(col, row));
			}
		}
		return tiles.stream();
	}

	default Vector2f center() {
		return Vector2f.of(((col() + 0.5f * width()) * Tile.SIZE), (row() + 0.5f * height()) * Tile.SIZE);
	}
}