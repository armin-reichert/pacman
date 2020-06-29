package de.amr.games.pacman.model.world;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.world.map.PacManWorldMap.B_EATEN;
import static de.amr.games.pacman.model.world.map.PacManWorldMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.map.PacManWorldMap.B_FOOD;
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
 * The Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
class PacManWorldUsingMap implements PacManWorld {

	private PacMan pacMan;
	private Ghost blinky, pinky, inky, clyde;
	private Bonus bonus;

	private final Set<Creature<?>> stage = new HashSet<>();
	private PacManWorldMap worldMap;
	private int totalFoodCount;

	public PacManWorldUsingMap(PacManWorldMap worldMap) {
		this.worldMap = worldMap;
		totalFoodCount = (int) habitatTiles().filter(this::containsFood).count();

		// birth
		pacMan = new PacMan();
		blinky = new Ghost("Blinky");
		inky = new Ghost("Inky");
		pinky = new Ghost("Pinky");
		clyde = new Ghost("Clyde");
		bonus = new Bonus();

		// put the creatures into this world
		creatures().forEach(creature -> creature.putIntoWorld(this));

		// assign beds
		pacMan.assignBed(pacManHome());
		House theHouse = theHouse();
		blinky.assignBed(theHouse.seat(0));
		inky.assignBed(theHouse.seat(1));
		pinky.assignBed(theHouse.seat(2));
		clyde.assignBed(theHouse.seat(3));

		// define behavior
		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		// common ghost behavior
		ghosts().forEach(ghost -> {
			ghost.behavior(LOCKED, ghost::bouncingOnSeat);
			ghost.behavior(ENTERING_HOUSE, ghost.isTakingSeat());
			ghost.behavior(LEAVING_HOUSE, ghost::leavingGhostHouse);
			ghost.behavior(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack());
			ghost.behavior(DEAD, ghost.isReturningToHouse());
		});

		// individual ghost behavior
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(theHouse.seat(2)));

		// scattering behavior
		int w = width(), h = height();
		blinky.behavior(SCATTERING, blinky.isHeadingFor(Tile.at(w - 3, 0)));
		inky.behavior(SCATTERING, inky.isHeadingFor(Tile.at(w - 1, h - 1)));
		pinky.behavior(SCATTERING, pinky.isHeadingFor(Tile.at(2, 0)));
		clyde.behavior(SCATTERING, clyde.isHeadingFor(Tile.at(0, h - 1)));

		// chasing behavior
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		clyde.behavior(CHASING, clyde.isHeadingFor(() -> clyde.distance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, h - 1)));
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
	public Bed pacManHome() {
		return worldMap.pacManSeat();
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

	// food container

	@Override
	public int totalFoodCount() {
		return totalFoodCount;
	}

	@Override
	public void removeFood() {
		habitatTiles().forEach(this::eatFood);
	}

	@Override
	public void createFood() {
		habitatTiles().forEach(this::restoreFood);
	}

	@Override
	public boolean containsFood(Tile tile) {
		return worldMap.is(tile, B_FOOD) && !worldMap.is(tile, B_EATEN);
	}

	@Override
	public boolean containsEatenFood(Tile tile) {
		return worldMap.is(tile, B_FOOD) && worldMap.is(tile, B_EATEN);
	}

	@Override
	public boolean containsSimplePellet(Tile tile) {
		return containsFood(tile) && !worldMap.is(tile, B_ENERGIZER);
	}

	@Override
	public boolean containsEnergizer(Tile tile) {
		return containsFood(tile) && worldMap.is(tile, B_ENERGIZER);
	}

	@Override
	public void eatFood(Tile tile) {
		if (worldMap.is(tile, B_FOOD)) {
			worldMap.set(tile, B_EATEN);
		}
	}

	@Override
	public void restoreFood(Tile tile) {
		if (worldMap.is(tile, B_FOOD)) {
			worldMap.clear(tile, B_EATEN);
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
}