package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.List;

public class Door {

	public final Direction dirIntoHouse;
	public final List<Tile> tiles;

	public Door(Direction dirIntoHouse, Tile... tiles) {
		this.tiles = Arrays.asList(tiles);
		this.dirIntoHouse = dirIntoHouse;
	}

	public boolean contains(Tile tile) {
		return tiles.contains(tile);
	}
}