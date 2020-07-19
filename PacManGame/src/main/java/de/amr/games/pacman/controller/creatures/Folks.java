package de.amr.games.pacman.controller.creatures;

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
import de.amr.games.pacman.model.world.api.Door;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * The folks from the original game with their individual behaviors.
 * 
 * @author Armin Reichert
 */
public class Folks {

	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	public Folks(World world) {
		House ghostHouse = world.house(0);
		Door door = ghostHouse.door(0);
		Tile houseEntry = world.neighbor(door.tiles().findFirst().get(), door.intoHouse.opposite());

		pacMan = new PacMan(world);
		you(pacMan).followTheKeys().keys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT).ok();

		blinky = new Ghost(world, pacMan, "Blinky", Ghost.RED_GHOST, ghostHouse.bed(0));
		inky = new Ghost(world, pacMan, "Inky", Ghost.CYAN_GHOST, ghostHouse.bed(1));
		pinky = new Ghost(world, pacMan, "Pinky", Ghost.PINK_GHOST, ghostHouse.bed(2));
		clyde = new Ghost(world, pacMan, "Clyde", Ghost.ORANGE_GHOST, ghostHouse.bed(3));

		ghosts().forEach(ghost -> {
			you(ghost).when(LOCKED).bounceOnBed().ok();
			you(ghost).when(ENTERING_HOUSE).enterHouseAndGoToBed().ok();
			you(ghost).when(LEAVING_HOUSE).leaveHouse().house(ghostHouse).ok();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			you(ghost).when(DEAD).headFor().tile(houseEntry).ok();
		});

		you(blinky).when(ENTERING_HOUSE).enterHouseAndGoToBed().bed(ghostHouse.bed(2)).ok();
		you(blinky).when(SCATTERING).headFor().tile(world.width() - 3, 0).ok();
		you(blinky).when(CHASING).headFor().tile(pacMan::tileLocation).ok();

		you(inky).when(SCATTERING).headFor().tile(world.width() - 1, world.height() - 1).ok();
		you(inky).when(CHASING).headFor().tile(() -> {
			Tile b = blinky.tileLocation(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).ok();

		you(pinky).when(SCATTERING).headFor().tile(2, 0).ok();
		you(pinky).when(CHASING).headFor().tile(() -> pacMan.tilesAhead(4)).ok();

		you(clyde).when(SCATTERING).headFor().tile(0, world.height() - 1).ok();
		you(clyde).when(CHASING).headFor()
				.tile(() -> clyde.distance(pacMan) > 8 ? pacMan.tileLocation() : Tile.at(0, world.height() - 1)).ok();
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Creature<?>> all() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}
}