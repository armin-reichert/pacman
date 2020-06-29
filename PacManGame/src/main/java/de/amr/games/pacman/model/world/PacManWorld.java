package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;

/**
 * Defines the structure of the Pac-man game world.
 * 
 * @author Armin Reichert
 */
public interface PacManWorld {

	PacMan pacMan();

	Ghost blinky();

	Ghost inky();

	Ghost pinky();

	Ghost clyde();

	Stream<Ghost> ghosts();

	Stream<Ghost> ghostsOnStage();

	Stream<Creature<?>> creatures();

	Stream<Creature<?>> creaturesOnStage();

	Bonus bonus();

	Stream<Tile> mapTiles();

	boolean takesPart(Creature<?> actor);

	void takePart(Creature<?> actor, boolean takesPart);

	/**
	 * @return world width in number of tiles
	 */
	int width();

	/**
	 * @return world height in number of tiles
	 */
	int height();

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

	boolean isTunnel(Tile tile);

	/**
	 * @param tile some tile
	 * @param dir  some direction
	 * @return {@code true} if this tile is a one-way tile to the given direction
	 */
	default boolean isOneWayTile(Tile tile, Direction dir) {
		return oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile) && oneWay.dir == dir);
	}

	int totalFoodCount();

	void eatFood();

	void restoreFood();

	boolean containsFood(Tile tile);

	boolean containsEatenFood(Tile tile);

	boolean containsEnergizer(Tile tile);

	boolean containsSimplePellet(Tile tile);

	void eatFood(Tile tile);
}