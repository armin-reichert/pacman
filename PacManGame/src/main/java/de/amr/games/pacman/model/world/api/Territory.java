package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.OneWayTile;
import de.amr.games.pacman.model.world.core.Portal;
import de.amr.games.pacman.model.world.core.Tile;

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
	 * @return Pac-Man's sleep location
	 */
	Bed pacManBed();

	/**
	 * @param tile some tile
	 * @return if there is a door at this tile or the tile is located inside a house
	 */
	boolean insideHouseOrDoor(Tile tile);

	/**
	 * @param tile some tile
	 * @return if this tile lies outside a house and a door to the house is its neighbor
	 */
	boolean isJustBeforeDoor(Tile tile);

	/**
	 * @return the portals in this territory
	 */
	Stream<Portal> portals();

	/**
	 * @param tile some tile
	 * @return if there is some portal at this tile
	 */
	default boolean anyPortalContains(Tile tile) {
		return portals().anyMatch(portal -> portal.includes(tile));
	}

	/**
	 * @param tile some tile
	 * @param dir  some direction
	 * @return if this tile can only get traversed in the given direction
	 */
	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	/**
	 * @return all one-way tiles in this territory
	 */
	Stream<OneWayTile> oneWayTiles();

	/**
	 * @return the bonus location of this territory
	 */
	Tile bonusTile();
}