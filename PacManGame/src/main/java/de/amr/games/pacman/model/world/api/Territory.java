package de.amr.games.pacman.model.world.api;

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;

public interface Territory {

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
	boolean isDoorAt(Tile tile);

	/**
	 * Part of the territory where creatures live.
	 * 
	 * @return stream of tiles constituting the habitat
	 */
	Stream<Tile> habitat();

	/**
	 * @param life a life
	 * @return {@code true} if the life is currently included
	 */
	boolean contains(Life life);

	/**
	 * Includes the life into the territory.
	 * 
	 * @param life a life
	 */
	void include(Life life);

	/**
	 * Temporarily excludes the life from the territory.
	 * 
	 * @param life a life
	 */
	void exclude(Life life);

	/**
	 * @return list of "capes" in order NW, NE, SE, SW
	 */
	List<Tile> capes();

	/**
	 * @return Pac-Man's sleep location
	 */
	Bed pacManBed();

	/**
	 * @return the houses in this territory
	 */
	Stream<House> houses();

	/**
	 * @param i index
	 * @return i'th house
	 */
	House house(int i);

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
	default boolean isPortalAt(Tile tile) {
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
}