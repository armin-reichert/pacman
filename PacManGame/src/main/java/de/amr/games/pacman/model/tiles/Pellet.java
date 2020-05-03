package de.amr.games.pacman.model.tiles;

public class Pellet extends Tile {

	public boolean eaten;
	public boolean energizer;

	public Pellet(int col, int row) {
		super(col, row);
		eaten = false;
		energizer = false;
	}
}