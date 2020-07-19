package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

public interface Territory extends RectangularArea {

	/**
	 * @return outmost accessible tile at north-west
	 */
	Tile capeNW();

	/**
	 * @return outmost accessible tile at north-east
	 */
	Tile capeNE();

	/**
	 * @return outmost accessible tile at south-west
	 */
	Tile capeSW();

	/**
	 * @return outmost accessible tile at south-east
	 */
	Tile capeSE();

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    some non-negative number
	 * @return the tile that is located n tiles away from the reference tile into the given direction
	 */
	Tile tileToDir(Tile tile, Direction dir, int n);

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return the neighbor of the reference tile into the given direction
	 */
	default Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	/**
	 * @param tile some tile
	 * @return if this tile has a least three accessible neighbor tiles
	 */
	boolean isIntersection(Tile tile);

	/**
	 * @param tile some tile
	 * @return if this tile is accessible
	 */
	boolean isAccessible(Tile tile);

	/**
	 * @param tile some tile
	 * @return if this tile is located inside a tunnel
	 */
	boolean isTunnel(Tile tile);

	/**
	 * @param tile some tile
	 * @return if a door is located at this tile
	 */
	boolean isDoor(Tile tile);

	/**
	 * Part of the territoty where creatures live.
	 * 
	 * @return stream of tiles of habitat area
	 */
	Stream<Tile> habitatArea();

	/**
	 * @param creature a creature
	 * @return {@code true} if the creature is currently included
	 */
	boolean contains(Lifeform creature);

	/**
	 * Includes the creature into the territory.
	 * 
	 * @param creature a creature
	 */
	void include(Lifeform creature);

	/**
	 * Temporarily excludes the creature from the territory.
	 * 
	 * @param creature a creature
	 */
	void exclude(Lifeform creature);

	/**
	 * @return Pac-Man's sleep location
	 */
	Bed pacManBed();

	/**
	 * @return the houses in this territory
	 */
	Stream<House> houses();

	/**
	 * @return the single house if there is only one
	 */
	default House theHouse() {
		return houses().findFirst().get();
	}

	/**
	 * @param tile some tile
	 * @return if there is a door at this tile or the tile is located inside a house
	 */
	boolean insideHouseOrDoor(Tile tile);

	/**
	 * @param tile some tile
	 * @return if this tile is the entry to a house
	 */
	boolean isHouseEntry(Tile tile);

	/**
	 * @return all portals in this territory
	 */
	Stream<Portal> portals();

	/**
	 * @param tile some tile
	 * @return if there is some portal at this tile
	 */
	default boolean isInsidePortal(Tile tile) {
		return portals().anyMatch(portal -> portal.includes(tile));
	}

	/**
	 * @return all one-way tiles in this territory
	 */
	Stream<OneWayTile> oneWayTiles();

	/**
	 * @param tile some tile
	 * @param dir  some direction
	 * @return if this tile can only get traversed in the given direction
	 */
	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	/**
	 * @return the bonus location of this territory
	 */
	Tile bonusTile();
}