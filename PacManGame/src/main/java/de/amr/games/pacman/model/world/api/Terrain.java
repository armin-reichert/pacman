package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.OneWayTile;
import de.amr.games.pacman.model.world.core.Portal;
import de.amr.games.pacman.model.world.core.Tile;

public interface Terrain extends RectangularArea {

	@Override
	int width();

	@Override
	int height();

	@Override
	public boolean includes(Tile tile);

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

	Tile tileToDir(Tile tile, Direction dir, int n);

	Tile neighbor(Tile tile, Direction dir);

	boolean isIntersection(Tile tile);

	boolean isAccessible(Tile tile);

	Stream<House> houses();

	default House theHouse() {
		return houses().findFirst().get();
	}

	Bed pacManBed();

	boolean isDoor(Tile tile);

	boolean insideHouseOrDoor(Tile tile);

	boolean isJustBeforeDoor(Tile tile);

	Stream<Portal> portals();

	default boolean anyPortalContains(Tile tile) {
		return portals().anyMatch(portal -> portal.includes(tile));
	}

	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	Stream<OneWayTile> oneWayTiles();

	boolean isTunnel(Tile tile);

	Tile bonusTile();
}