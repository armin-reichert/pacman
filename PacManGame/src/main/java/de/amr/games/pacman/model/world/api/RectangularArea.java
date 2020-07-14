package de.amr.games.pacman.model.world.api;

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
}