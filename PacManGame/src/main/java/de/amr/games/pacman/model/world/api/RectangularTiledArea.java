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