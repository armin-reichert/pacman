package de.amr.games.pacman.model.world;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.model.Direction;

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