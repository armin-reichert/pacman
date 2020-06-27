package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.Tile;

public class Portal {
	public Tile left, right;

	public boolean contains(Tile tile) {
		return tile.equals(left) || tile.equals(right);
	}
}