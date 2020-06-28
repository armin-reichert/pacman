package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;

/**
 * Defines the structure of the Pac-man game world.
 * 
 * @author Armin Reichert
 */
public interface PacManWorldStructure {

	/**
	 * @return world width in number of tiles
	 */
	int width();

	/**
	 * @return world height in number of tiles
	 */
	int height();
	
	/**
	 * @return houses in world
	 */
	Stream<House> houses();

	/**
	 * @return the single house
	 */
	default House theHouse() {
		return houses().findFirst().get();
	}

	/**
	 * @return Pac-Man's seat
	 */
	Seat pacManSeat();

	/**
	 * @return bonus tile location
	 */
	Tile bonusTile();

	/**
	 * @return the portals in the world
	 */
	Stream<Portal> portals();

	/**
	 * @param tile some tile
	 * @return {@code true} if this tile is located inside a portal
	 */
	default boolean anyPortalContains(Tile tile) {
		return portals().anyMatch(portal -> portal.contains(tile));
	}

	/**
	 * @return the one-way tiles in the world
	 */
	Stream<OneWayTile> oneWayTiles();

	/**
	 * @param tile some tile
	 * @param dir  some direction
	 * @return {@code true} if this tile is a one-way tile to the given direction
	 */
	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}
}