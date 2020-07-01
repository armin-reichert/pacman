package de.amr.games.pacman.model.world.core;

import de.amr.games.pacman.model.world.api.RectangularArea;

public class Block implements RectangularArea {

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	public Block(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	@Override
	public boolean includes(Tile tile) {
		return x <= tile.col && tile.col < x + width && y <= tile.row && tile.row <= y + height;
	}
}