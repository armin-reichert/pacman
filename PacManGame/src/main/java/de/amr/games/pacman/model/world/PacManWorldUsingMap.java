package de.amr.games.pacman.model.world;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.map.PacManWorldMap;

/**
 * Map-based Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
class PacManWorldUsingMap implements PacManWorld {

	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Bonus bonus;
	private final Set<Creature<?>> stage = new HashSet<>();
	private PacManWorldMap worldMap;

	public PacManWorldUsingMap(PacManWorldMap worldMap) {
		this();
		setWorldMap(worldMap);
	}

	public PacManWorldUsingMap() {
		pacMan = new PacMan();
		blinky = new Ghost("Blinky");
		inky = new Ghost("Inky");
		pinky = new Ghost("Pinky");
		clyde = new Ghost("Clyde");
		bonus = new Bonus();
	}

	public void setWorldMap(PacManWorldMap worldMap) {
		this.worldMap = worldMap;
		creatures().forEach(creature -> creature.setWorld(this));
		assignBeds();
		defineCreatureBehaviors();
	}

	private void assignBeds() {
		pacMan.assignBed(pacManBed());
		blinky.assignBed(theHouse().bed(0));
		inky.assignBed(theHouse().bed(1));
		pinky.assignBed(theHouse().bed(2));
		clyde.assignBed(theHouse().bed(3));
	}

	private void defineCreatureBehaviors() {
		pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		ghosts().forEach(ghost -> {
			ghost.behavior(LOCKED, ghost::bouncingOnBed);
			ghost.behavior(ENTERING_HOUSE, ghost.isGoingToBed(ghost.bed()));
			ghost.behavior(LEAVING_HOUSE, ghost::leavingGhostHouse);
			ghost.behavior(FRIGHTENED, ghost.movingRandomly());
			ghost.behavior(DEAD, ghost.isReturningToHouse());
		});
		blinky.behavior(ENTERING_HOUSE, blinky.isGoingToBed(theHouse().bed(2)));
		int w = width(), h = height();
		blinky.behavior(SCATTERING, blinky.headingFor(Tile.at(w - 3, 0)));
		inky.behavior(SCATTERING, inky.headingFor(Tile.at(w - 1, h - 1)));
		pinky.behavior(SCATTERING, pinky.headingFor(Tile.at(2, 0)));
		clyde.behavior(SCATTERING, clyde.headingFor(Tile.at(0, h - 1)));
		blinky.behavior(CHASING, blinky.headingFor(pacMan::tile));
		inky.behavior(CHASING, inky.headingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		pinky.behavior(CHASING, pinky.headingFor(() -> pacMan.tilesAhead(4)));
		clyde.behavior(CHASING, clyde.headingFor(() -> clyde.distance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, h - 1)));
	}

	// habitat

	/**
	 * @return the habitat tiles
	 */
	@Override
	public Stream<Tile> habitatTiles() {
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
	}

	@Override
	public PacMan pacMan() {
		return pacMan;
	}

	@Override
	public Ghost blinky() {
		return blinky;
	}

	@Override
	public Ghost pinky() {
		return pinky;
	}

	@Override
	public Ghost inky() {
		return inky;
	}

	@Override
	public Ghost clyde() {
		return clyde;
	}

	/**
	 * @return stream of all ghosts
	 */
	@Override
	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of ghosts currently on stage
	 */
	@Override
	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(stage::contains);
	}

	/**
	 * @return stream of all creatures (ghosts and Pac-Man)
	 */
	@Override
	public Stream<Creature<?>> creatures() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of creatures currently on stage (ghosts and Pac-Man)
	 */
	@Override
	public Stream<Creature<?>> creaturesOnStage() {
		return creatures().filter(stage::contains);
	}

	/**
	 * @param actor a ghost or Pac-Man
	 * @return {@code true} if the actor is currently on stage
	 */
	@Override
	public boolean isOnStage(Creature<?> actor) {
		return stage.contains(actor);
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
	public Bonus bonus() {
		return bonus;
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