package de.amr.games.pacman.model.map;

import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Tile;

public class CustomMap extends ArcadeMap {

	public CustomMap() {
		addPortal(Tile.at(0, 4), Tile.at(27, 32));
		addPortal(Tile.at(0, 32), Tile.at(27, 4));
	}

	void addPortal(Tile left, Tile right) {
		set0(left.row, left.col, B_WALL);
		set1(left.row, left.col, B_TUNNEL);
		set0(right.row, right.col, B_WALL);
		set1(right.row, right.col, B_TUNNEL);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}
}