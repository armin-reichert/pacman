package de.amr.games.pacman.controller.world.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

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

		pacMan = new PacMan(world);
		blinky = new Ghost(world, "Blinky", Ghost.RED_GHOST);
		inky = new Ghost(world, "Inky", Ghost.CYAN_GHOST);
		pinky = new Ghost(world, "Pinky", Ghost.PINK_GHOST);
		clyde = new Ghost(world, "Clyde", Ghost.ORANGE_GHOST);

		you(pacMan).followTheKeys().keys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT).ok();

		House house = world.theHouse();
		Tile houseEntry = Tile.at(house.bed(0).col(), house.bed(0).row());

		blinky.setBed(house.bed(0));
		inky.setBed(house.bed(1));
		pinky.setBed(house.bed(2));
		clyde.setBed(house.bed(3));

		ghosts().forEach(ghost -> {
			you(ghost).when(LOCKED).bounceOnBed().ok();
			you(ghost).when(ENTERING_HOUSE).enterHouseAndGoToBed().ok();
			you(ghost).when(LEAVING_HOUSE).leaveHouse().house(house).ok();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			you(ghost).when(DEAD).headFor().tile(houseEntry).ok();
		});

		you(blinky).when(ENTERING_HOUSE).enterHouseAndGoToBed().bed(house.bed(2)).ok();
		you(blinky).when(SCATTERING).headFor().tile(world.width() - 3, 0).ok();
		you(blinky).when(CHASING).headFor().tile(pacMan::location).ok();

		you(inky).when(SCATTERING).headFor().tile(world.width() - 1, world.height() - 1).ok();
		you(inky).when(CHASING).headFor().tile(() -> {
			Tile b = blinky.location(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).ok();

		you(pinky).when(SCATTERING).headFor().tile(2, 0).ok();
		you(pinky).when(CHASING).headFor().tile(() -> pacMan.tilesAhead(4)).ok();

		you(clyde).when(SCATTERING).headFor().tile(0, world.height() - 1).ok();
		you(clyde).when(CHASING).headFor()
				.tile(() -> clyde.distance(pacMan) > 8 ? pacMan.location() : Tile.at(0, world.height() - 1)).ok();
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

	public Stream<Creature<?>> all() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}

	public Stream<Ghost> ghostsInsideWorld() {
		return ghosts().filter(world::contains);
	}
}