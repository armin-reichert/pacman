package de.amr.games.pacman.model.world.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * The Pac-Man game world is a territory where creatures can live and get food.
 * 
 * @author Armin Reichert
 */
public interface World extends RectangularArea, FoodSource {

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    some non-negative number
	 * @return the tile reached after going n tiles to the given direction
	 */
	Tile tileToDir(Tile tile, Direction dir, int n);

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return the direct neighbor to the given direction
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
	 * @return if there is a tunnel at this tile
	 */
	boolean isTunnel(Tile tile);

	/**
	 * @return list of "capes" (outmost reachable tiles) in order NW, NE, SE, SW
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
	 * @return i'th house in this territory
	 */
	Optional<House> house(int i);

	/**
	 * @return all portals in this territory
	 */
	Stream<Portal> portals();

	/**
	 * @param tile some tile location
	 * @return if there is some portal at this tile
	 */
	default boolean isPortal(Tile tile) {
		return portals().anyMatch(portal -> portal.includes(tile));
	}

	/**
	 * @return all one-way tiles in this territory
	 */
	Stream<OneWayTile> oneWayTiles();

	/**
	 * @param tile some tile location
	 * @param dir  some direction
	 * @return if this tile can only get traversed in the given direction
	 */
	default boolean isOneWay(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	/**
	 * Signals that the world is changing.
	 * 
	 * @param changing if the world is changing
	 */
	void setChanging(boolean changing);

	/**
	 * @return if the world is just changing
	 */
	boolean isChanging();

	/**
	 * Sets the world into "frozen" state where the creatures do not move and are not animated.
	 * 
	 * @param frozen if the world is frozen
	 */
	void setFrozen(boolean frozen);

	/**
	 * @return if the world is frozen
	 */
	boolean isFrozen();

	/**
	 * @param entity an entity
	 * @return {@code true} if the entity is currently included in this territory
	 */
	boolean contains(Entity entity);

	/**
	 * Includes the entity into the territory.
	 * 
	 * @param entity an entity
	 */
	void include(Entity entity);

	/**
	 * Excludes the entity from the territory.
	 * 
	 * @param entity an entity
	 */
	void exclude(Entity entity);
}