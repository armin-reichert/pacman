package de.amr.games.pacman.model.world;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.world.map.PacManMap.B_EATEN;
import static de.amr.games.pacman.model.world.map.PacManMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.map.PacManMap.B_FOOD;
import static de.amr.games.pacman.model.world.map.PacManMap.B_INTERSECTION;
import static de.amr.games.pacman.model.world.map.PacManMap.B_TUNNEL;
import static de.amr.games.pacman.model.world.map.PacManMap.B_WALL;
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
import de.amr.games.pacman.model.world.map.PacManMap;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class PacManWorld implements PacManWorldStructure {

	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;
	public final Bonus bonus;

	private PacManMap map;
	private Set<Creature<?>> actorsTakingPart = new HashSet<>();

	public PacManWorld(PacManMap map) {
		this.map = map;

		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");
		bonus = new Bonus();

		pacMan = new PacMan(this);

		House theHouse = theHouse();
		blinky.seat = theHouse.seat(0);
		inky.seat = theHouse.seat(1);
		pinky.seat = theHouse.seat(2);
		clyde.seat = theHouse.seat(3);

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

		// scattering

		int w = width(), h = height();
		blinky.behavior(SCATTERING, blinky.isHeadingFor(Tile.at(w - 3, 0)));
		inky.behavior(SCATTERING, inky.isHeadingFor(Tile.at(w - 1, h - 1)));
		pinky.behavior(SCATTERING, pinky.isHeadingFor(Tile.at(2, 0)));
		clyde.behavior(SCATTERING, clyde.isHeadingFor(Tile.at(0, h - 1)));

		// chasing

		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		clyde.behavior(CHASING, clyde.isHeadingFor(() -> clyde.distance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, h - 1)));
	}

	/**
	 * @return stream of all ghosts
	 */
	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of ghosts currently on stage
	 */
	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(actorsTakingPart::contains);
	}

	/**
	 * @return stream of all creatures (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> creatures() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of creatures currently on stage (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> creaturesOnStage() {
		return creatures().filter(actorsTakingPart::contains);
	}

	/**
	 * @param actor a ghost or Pac-Man
	 * @return {@code true} if the actor is currently on stage
	 */
	public boolean takesPart(Creature<?> actor) {
		return actorsTakingPart.contains(actor);
	}

	/**
	 * Lets the actor take part at the game.
	 * 
	 * @param actor     a ghost or Pac-Man
	 * @param takesPart if the actors takes part
	 */
	public void takePart(Creature<?> actor, boolean takesPart) {
		if (takesPart) {
			actorsTakingPart.add(actor);
			actor.init();
			actor.visible = true;
			loginfo("%s entered the game", actor.name);
		} else {
			actorsTakingPart.remove(actor);
			actor.visible = false;
			actor.placeAt(Tile.at(-1, -1));
			loginfo("%s left the game", actor.name);

		}
	}

	public void takePart(Creature<?> actor) {
		takePart(actor, true);
	}

	@Override
	public int width() {
		return map.width();
	}

	@Override
	public int height() {
		return map.height();
	}

	public boolean insideMap(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
	}

	@Override
	public Stream<House> houses() {
		return map.houses();
	}

	@Override
	public Seat pacManSeat() {
		return map.pacManSeat();
	}

	@Override
	public Tile bonusTile() {
		return map.bonusTile();
	}

	@Override
	public Stream<Portal> portals() {
		return map.portals();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return map.oneWayTiles();
	}

	/**
	 * @return the map tiles in world coordinates
	 */
	public Stream<Tile> mapTiles() {
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the world.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		//@formatter:off
		return portals()
			.filter(portal -> portal.contains(tile))
			.findAny()
			.map(portal -> portal.exitTile(tile, dir))
			.orElse(Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY()));
		//@formatter:on
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return Neighbor towards the given direction. This can be a tile outside of the map.
	 */
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public Stream<Tile> neighbors(Tile tile) {
		return Direction.dirs().map(dir -> neighbor(tile, dir));
	}

	public void eatFood() {
		mapTiles().forEach(this::eatFood);
	}

	public void restoreFood() {
		mapTiles().forEach(this::restoreFood);
	}

	public boolean isDoor(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.contains(tile));
	}

	public boolean insideHouseOrDoor(Tile tile) {
		return isDoor(tile) || houses().map(House::room).anyMatch(room -> room.contains(tile));
	}

	public boolean outsideAtDoor(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoor(neighbor)) {
				Door door = houses().flatMap(House::doors).filter(d -> d.contains(neighbor)).findFirst().get();
				return door.intoHouse == dir;
			}
		}
		return false;
	}

	private boolean is(Tile tile, byte bit) {
		return insideMap(tile) && map.is(tile.row, tile.col, bit);
	}

	private void set(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set1(tile.row, tile.col, bit);
		}
	}

	private void clear(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set0(tile.row, tile.col, bit);
		}
	}

	public boolean isAccessible(Tile tile) {
		boolean inside = insideMap(tile);
		return inside && !is(tile, B_WALL) || !inside && anyPortalContains(tile);
	}

	public boolean isTunnel(Tile tile) {
		return is(tile, B_TUNNEL);
	}

	public boolean insidePortal(Tile tile) {
		return map.anyPortalContains(tile);
	}

	public boolean isIntersection(Tile tile) {
		return is(tile, B_INTERSECTION);
	}

	public boolean containsFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	public boolean containsEatenFood(Tile tile) {
		return is(tile, B_FOOD) && is(tile, B_EATEN);
	}

	public boolean isEnergizer(Tile tile) {
		return is(tile, B_ENERGIZER);
	}

	public boolean containsSimplePellet(Tile tile) {
		return containsFood(tile) && !isEnergizer(tile);
	}

	public boolean containsEnergizer(Tile tile) {
		return containsFood(tile) && isEnergizer(tile);
	}

	public void eatFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			set(tile, B_EATEN);
		}
	}

	public void restoreFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			clear(tile, B_EATEN);
		}
	}
}