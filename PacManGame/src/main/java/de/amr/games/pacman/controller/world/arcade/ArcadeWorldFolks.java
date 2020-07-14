package de.amr.games.pacman.controller.world.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.enteringHouseAndGoingToBed;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.headingForTargetTile;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.randomMovement;
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
			ghost.behavior(LEAVING_HOUSE, () -> ghost.leavingHouse(house));
			ghost.behavior(FRIGHTENED, randomMovement(ghost).build());
			ghost.behavior(DEAD, headingForTargetTile(ghost).tile(houseEntry).build());
			ghost.behavior(DEAD, headingForTargetTile(ghost).tile(houseEntry).build());
		});

		blinky.behavior(LOCKED, () -> blinky.bouncingOnBed(house.bed(0)));
		blinky.behavior(ENTERING_HOUSE, enteringHouseAndGoingToBed(blinky).bed(house.bed(2)).build());
		blinky.behavior(SCATTERING, headingForTargetTile(blinky).tile(worldWidth - 3, 0).build());
		blinky.behavior(CHASING, headingForTargetTile(blinky).tile(pacMan::location).build());

		inky.behavior(LOCKED, () -> inky.bouncingOnBed(house.bed(1)));
		inky.behavior(ENTERING_HOUSE, enteringHouseAndGoingToBed(inky).bed(house.bed(1)).build());
		inky.behavior(SCATTERING, headingForTargetTile(inky).tile(worldWidth - 1, worldHeight - 1).build());
		inky.behavior(CHASING, headingForTargetTile(inky).tile(() -> {
			Tile b = blinky.location(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).build());

		pinky.behavior(LOCKED, () -> pinky.bouncingOnBed(house.bed(2)));
		pinky.behavior(ENTERING_HOUSE, enteringHouseAndGoingToBed(pinky).bed(house.bed(2)).build());
		pinky.behavior(SCATTERING, headingForTargetTile(pinky).tile(2, 0).build());
		pinky.behavior(CHASING, headingForTargetTile(pinky).tile(() -> pacMan.tilesAhead(4)).build());

		clyde.behavior(LOCKED, () -> clyde.bouncingOnBed(house.bed(3)));
		clyde.behavior(ENTERING_HOUSE, enteringHouseAndGoingToBed(clyde).bed(house.bed(3)).build());
		clyde.behavior(SCATTERING, headingForTargetTile(clyde).tile(0, worldHeight - 1).build());
		clyde.behavior(CHASING, headingForTargetTile(clyde)
				.tile(() -> clyde.distance(pacMan) > 8 ? pacMan.location() : Tile.at(0, worldHeight - 1)).build());
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