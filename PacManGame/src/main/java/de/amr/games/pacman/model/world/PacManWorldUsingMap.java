package de.amr.games.pacman.model.world;

import static de.amr.easy.game.Application.loginfo;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.map.PacManWorldMap;

/**
 * Map-based Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
class PacManWorldUsingMap implements PacManWorld {

	private final Set<Creature<?>> stage = new HashSet<>();
	private PacManWorldMap worldMap;
	private Population population;

	public PacManWorldUsingMap(PacManWorldMap worldMap) {
		setWorldMap(worldMap);
	}

	public void setWorldMap(PacManWorldMap worldMap) {
		this.worldMap = worldMap;
	}

	@Override
	public Population population() {
		return population;
	}

	@Override
	public void accept(Population aliens) {
		this.population = aliens;
		aliens.pacMan().assignBed(pacManBed());
		aliens.blinky().assignBed(theHouse().bed(0));
		aliens.inky().assignBed(theHouse().bed(1));
		aliens.pinky().assignBed(theHouse().bed(2));
		aliens.clyde().assignBed(theHouse().bed(3));
	}

	// habitat

	/**
	 * @return the habitat tiles
	 */
	@Override
	public Stream<Tile> habitatTiles() {
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
	}

	/**
	 * Lets the actor take part at the game.
	 * 
	 * @param actor     a ghost or Pac-Man
	 * @param takesPart if the actors takes part
	 */
	@Override
	public void putOnStage(Creature<?> actor, boolean takesPart) {
		if (takesPart) {
			stage.add(actor);
			actor.init();
			actor.visible = true;
			loginfo("%s entered the game", actor.name);
		} else {
			stage.remove(actor);
			actor.visible = false;
			actor.placeAt(Tile.at(-1, -1));
			loginfo("%s left the game", actor.name);

		}
	}

	@Override
	public boolean isOnStage(Creature<?> creature) {
		return stage.contains(creature);
	}

	// terrain

	@Override
	public int width() {
		return worldMap.width();
	}

	@Override
	public int height() {
		return worldMap.height();
	}

	@Override
	public boolean contains(Tile tile) {
		return worldMap.contains(tile);
	}

	@Override
	public boolean isAccessible(Tile tile) {
		return worldMap.isAccessible(tile);
	}

	@Override
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		return worldMap.tileToDir(tile, dir, n);
	}

	@Override
	public Tile neighbor(Tile tile, Direction dir) {
		return worldMap.neighbor(tile, dir);
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return worldMap.isIntersection(tile);
	}

	@Override
	public Stream<House> houses() {
		return worldMap.houses();
	}

	@Override
	public Bed pacManBed() {
		return worldMap.pacManBed();
	}

	@Override
	public boolean insideHouseOrDoor(Tile tile) {
		return worldMap.insideHouseOrDoor(tile);
	}

	@Override
	public Stream<Portal> portals() {
		return worldMap.portals();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return worldMap.oneWayTiles();
	}

	@Override
	public boolean isTunnel(Tile tile) {
		return worldMap.isTunnel(tile);
	}

	@Override
	public Tile bonusTile() {
		return worldMap.bonusTile();
	}

	@Override
	public boolean isDoor(Tile tile) {
		return worldMap.isDoor(tile);
	}

	@Override
	public boolean isJustBeforeDoor(Tile tile) {
		return worldMap.isJustBeforeDoor(tile);
	}

	// food container

	@Override
	public boolean containsEatenFood(Tile tile) {
		return worldMap.containsEatenFood(tile);
	}

	@Override
	public boolean containsEnergizer(Tile tile) {
		return worldMap.containsEnergizer(tile);
	}

	@Override
	public boolean containsFood(Tile tile) {
		return worldMap.containsFood(tile);
	}

	@Override
	public boolean containsSimplePellet(Tile tile) {
		return worldMap.containsSimplePellet(tile);
	}

	@Override
	public void createFood(Tile tile) {
		worldMap.createFood(tile);
	}

	@Override
	public void createFood() {
		worldMap.createFood();
	}

	@Override
	public void removeFood(Tile tile) {
		worldMap.removeFood(tile);
	}

	@Override
	public void removeFood() {
		worldMap.removeFood();

	}

	@Override
	public int totalFoodCount() {
		return worldMap.totalFoodCount();
	}
}