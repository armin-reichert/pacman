package de.amr.games.pacman.model.world.api;

public class Block implements RectangularArea {

	private final int col;
	private final int row;
	private final int width;
	private final int height;

	public Block(int col, int row, int width, int height) {
		this.col = col;
		this.row = row;
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
	public int col() {
		return col;
	}

	@Override
	public int row() {
		return row;
	}
}