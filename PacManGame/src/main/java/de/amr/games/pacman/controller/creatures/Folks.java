package de.amr.games.pacman.controller.creatures;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.you;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;

/**
 * The folks from the original game with their individual behaviors.
 * 
 * @author Armin Reichert
 */
public class Folks {

	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	public Folks(World world, House ghostHouse) {

		pacMan = new PacMan(world, "Pac-Man");

		blinky = Ghost.shadowOne(world, "Blinky");
		inky = Ghost.bashfulOne(world, "Inky");
		pinky = Ghost.speedyOne(world, "Pinky");
		clyde = Ghost.pokeyOne(world, "Clyde");

		ghosts().forEach(ghost -> {
			ghost.house = ghostHouse;
			ghost.pacMan = pacMan;
		});

		blinky.bed = ghostHouse.bed(0);
		inky.bed = ghostHouse.bed(1);
		pinky.bed = ghostHouse.bed(2);
		clyde.bed = ghostHouse.bed(3);

		// define behavior

		you(pacMan).followTheCursorKeys().ok();

		Door door = ghostHouse.door(0);
		Tile houseEntry = world.neighbor(door.tiles().findFirst().get(), door.intoHouse.opposite());

		ghosts().forEach(ghost -> {
			you(ghost).when(LOCKED).bounceOnBed().ok();
			you(ghost).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).ok();
			you(ghost).when(LEAVING_HOUSE).leaveHouse().house(ghostHouse).ok();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			you(ghost).when(DEAD).headFor().tile(houseEntry).ok();
		});

		you(blinky).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).bed(pinky.bed).ok();
		you(blinky).when(SCATTERING).headFor().tile(world.width() - 3, 0).ok();
		you(blinky).when(CHASING).headFor().tile(pacMan::tile).ok();

		you(inky).when(SCATTERING).headFor().tile(world.width() - 1, world.height() - 1).ok();
		you(inky).when(CHASING).headFor().tile(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).ok();

		you(pinky).when(SCATTERING).headFor().tile(2, 0).ok();
		you(pinky).when(CHASING).headFor().tile(() -> pacMan.tilesAhead(4)).ok();

		you(clyde).when(SCATTERING).headFor().tile(0, world.height() - 1).ok();
		you(clyde).when(CHASING).headFor()
				.tile(() -> clyde.tileDistance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, world.height() - 1)).ok();
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Guy<?>> guys() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsInWorld() {
		return ghosts().filter(ghost -> ghost.world.contains(ghost));
	}

	public Stream<Guy<?>> guysInWorld() {
		return guys().filter(guy -> guy.world.contains(guy));
	}
}