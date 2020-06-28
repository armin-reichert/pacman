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

	default boolean isPortal(Tile tile) {
		return portals().anyMatch(portal -> portal.contains(tile));
	}

	Stream<OneWayTile> oneWayTiles();

	default boolean isOneWayTile(Tile tile) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile));
	}
}
