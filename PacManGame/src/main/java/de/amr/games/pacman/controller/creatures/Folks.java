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

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
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

	public final World world;
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	public Folks(World world, House ghostHouse) {
		this.world = world;
		pacMan = new PacMan(world);
		blinky = new Ghost("Blinky", GhostPersonality.SHADOW, world);
		inky = new Ghost("Inky", GhostPersonality.BASHFUL, world);
		pinky = new Ghost("Pinky", GhostPersonality.SPEEDY, world);
		clyde = new Ghost("Clyde", GhostPersonality.POKEY, world);

		ghosts().forEach(ghost -> ghost.setPacMan(pacMan));

		blinky.assignBed(ghostHouse, 0);
		inky.assignBed(ghostHouse, 1);
		pinky.assignBed(ghostHouse, 2);
		clyde.assignBed(ghostHouse, 3);

		// define behavior

		you(pacMan).followTheKeys().keys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT).ok();

		Door door = ghostHouse.door(0);
		Tile houseEntry = world.neighbor(door.tiles().findFirst().get(), door.intoHouse.opposite());

		ghosts().forEach(ghost -> {
			you(ghost).when(LOCKED).bounceOnBed().ok();
			you(ghost).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).ok();
			you(ghost).when(LEAVING_HOUSE).leaveHouse().house(ghostHouse).ok();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			you(ghost).when(DEAD).headFor().tile(houseEntry).ok();
		});

		you(blinky).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).bed(pinky.bed()).ok();
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

	public void getReadyToRumble(Game game) {
		all().forEach(guy -> guy.setGame(game));
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsInWorld() {
		return ghosts().filter(ghost -> ghost.world().contains(ghost));
	}

	public Stream<Creature<?, ?>> all() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}

	public Stream<Creature<?, ?>> allInWorld() {
		return all().filter(guy -> guy.world().contains(guy));
	}
}