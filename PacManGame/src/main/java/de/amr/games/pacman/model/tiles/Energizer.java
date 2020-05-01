package de.amr.games.pacman.model.tiles;

public class Energizer extends Tile {

	public boolean eaten;

	public Energizer(int col, int row) {
		super(col, row);
		eaten = false;
	}
}
