package de.amr.games.pacman.controller.world.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.bouncesOnBed;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.entersHouseAndGoesToBed;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.ghost;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.leavesHouse;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.movesRandomly;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Animal;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.Tile;

/**
 * The folks from the original game with their individual behaviors.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorldFolks {

	private final World world;
	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;

	public ArcadeWorldFolks(ArcadeWorld world) {
		this.world = world;
		world.setFolks(this);

		pacMan = new PacMan();
		blinky = new Ghost(this, "Blinky", Ghost.RED_GHOST);
		inky = new Ghost(this, "Inky", Ghost.CYAN_GHOST);
		pinky = new Ghost(this, "Pinky", Ghost.PINK_GHOST);
		clyde = new Ghost(this, "Clyde", Ghost.ORANGE_GHOST);

		all().forEach(creature -> creature.setWorld(world));

		pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		int worldWidth = world.width(), worldHeight = world.height();
		House house = world.theHouse();
		Tile houseEntry = Tile.at(house.bed(0).col(), house.bed(0).row());

		ghosts().forEach(ghost -> {
			ghost.behavior(LEAVING_HOUSE, leavesHouse(ghost).house(house).ok());
			ghost.behavior(FRIGHTENED, movesRandomly(ghost).ok());
			ghost(ghost).when(DEAD).headsFor().tile(houseEntry).ok();
		});

		blinky.behavior(LOCKED, bouncesOnBed(blinky).bed(house.bed(0)).ok());
		blinky.behavior(ENTERING_HOUSE, entersHouseAndGoesToBed(blinky).bed(house.bed(2)).ok());
		ghost(blinky).when(SCATTERING).headsFor().tile(worldWidth - 3, 0).ok();
		ghost(blinky).when(CHASING).headsFor().tile(pacMan::location).ok();

		inky.behavior(LOCKED, bouncesOnBed(inky).bed(house.bed(1)).ok());
		inky.behavior(ENTERING_HOUSE, entersHouseAndGoesToBed(inky).bed(house.bed(1)).ok());
		ghost(inky).when(SCATTERING).headsFor().tile(worldWidth - 1, worldHeight - 1).ok();
		ghost(inky).when(CHASING).headsFor().tile(() -> {
			Tile b = blinky.location(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).ok();

		pinky.behavior(LOCKED, bouncesOnBed(pinky).bed(house.bed(1)).ok());
		pinky.behavior(ENTERING_HOUSE, entersHouseAndGoesToBed(pinky).bed(house.bed(2)).ok());
		ghost(pinky).when(SCATTERING).headsFor().tile(2, 0).ok();
		ghost(pinky).when(CHASING).headsFor().tile(() -> pacMan.tilesAhead(4)).ok();

		clyde.behavior(LOCKED, bouncesOnBed(clyde).bed(house.bed(3)).ok());
		clyde.behavior(ENTERING_HOUSE, entersHouseAndGoesToBed(clyde).bed(house.bed(3)).ok());
		ghost(clyde).when(SCATTERING).headsFor().tile(0, worldHeight - 1).ok();
		ghost(clyde).when(CHASING).headsFor()
				.tile(() -> clyde.distance(pacMan) > 8 ? pacMan.location() : Tile.at(0, worldHeight - 1)).ok();
	}

	public World world() {
		return world;
	}

	public PacMan pacMan() {
		return pacMan;
	}

	public Ghost blinky() {
		return blinky;
	}

	public Ghost inky() {
		return inky;
	}

	public Ghost pinky() {
		return pinky;
	}

	public Ghost clyde() {
		return clyde;
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Animal<?>> all() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}

	public Stream<Ghost> ghostsInsideWorld() {
		return ghosts().filter(world::contains);
	}
}