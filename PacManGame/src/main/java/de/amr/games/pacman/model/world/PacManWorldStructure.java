package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

/**
 * Defines the structure of the Pac-man game world.
 * 
 * @author Armin Reichert
 */
public interface PacManWorldStructure {

	int width();

	int height();

	Stream<House> houses();

	Seat pacManSeat();

	Tile bonusTile();

	Stream<Portal> portals();

	Stream<OneWayTile> oneWayTiles();
}
