package de.amr.games.pacman.model.map;

import de.amr.games.pacman.model.world.Tile;

public class CustomMap extends ArcadeMap {

	public CustomMap() {
		addPortal(Tile.at(0, 4), Tile.at(27, 32));
		addPortal(Tile.at(0, 32), Tile.at(27, 4));
		setEnergizer(Tile.at(1, 11));
		setEnergizer(Tile.at(26, 11));
		setEnergizer(Tile.at(1, 23));
		setEnergizer(Tile.at(26, 23));
	}
}