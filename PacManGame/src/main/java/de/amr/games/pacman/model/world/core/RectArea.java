package de.amr.games.pacman.model.world.core;

public class RectArea {

	public final int top;
	public final int left;
	public final int width;
	public final int height;

	public RectArea(int top, int left, int width, int height) {
		this.top = top;
		this.left = left;
		this.width = width;
		this.height = height;
	}

	public boolean includes(Tile tile) {
		return left <= tile.col && tile.col < left + width && top <= tile.row && tile.row <= top + height;
	}
}