package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;

public interface Terrain {

	/**
	 * @return width in number of tiles
	 */
	int width();

	/**
	 * @return height in number of tiles
	 */
	int height();

	public boolean contains(Tile tile);

	default Tile cornerNW() {
		return Tile.at(1, 4);
	}

	default Tile cornerNE() {
		return Tile.at(width() - 2, 4);
	}

	default Tile cornerSW() {
		return Tile.at(1, height() - 4);
	}

	default Tile cornerSE() {
		return Tile.at(width() - 2, height() - 4);
	}

	Tile tileToDir(Tile tile, Direction dir, int n);

	Tile neighbor(Tile tile, Direction dir);

	boolean isIntersection(Tile tile);

	boolean isAccessible(Tile tile);

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

	Bed pacManBed();

	boolean isDoor(Tile tile);

	boolean insideHouseOrDoor(Tile tile);

	boolean isJustBeforeDoor(Tile tile);

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
	 * @param tile some tile
	 * @param dir  some direction
	 * @return {@code true} if this tile is a one-way tile to the given direction
	 */
	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	/**
	 * @return the one-way tiles in the world
	 */
	Stream<OneWayTile> oneWayTiles();

	boolean isTunnel(Tile tile);

	/**
	 * @return bonus tile location
	 */
	Tile bonusTile();
}