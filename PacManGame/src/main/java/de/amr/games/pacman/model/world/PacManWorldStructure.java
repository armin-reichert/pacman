package de.amr.games.pacman.model.world;

import java.util.List;

public interface PacManWorldStructure {

	int width();

	int height();

	GhostHouse ghostHouse();

	Seat pacManSeat();

	Tile bonusTile();

	List<Portal> portals();
	
	List<OneWayTile> oneWayTiles();
}
