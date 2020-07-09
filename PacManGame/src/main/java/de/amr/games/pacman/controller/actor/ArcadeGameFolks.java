package de.amr.games.pacman.controller.actor;

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

import java.util.stream.Stream;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;

public class ArcadeGameFolks implements Population {

	private final PacMan pacMan = new PacMan();
	private final Ghost blinky = new Ghost("Blinky");
	private final Ghost pinky = new Ghost("Pinky");
	private final Ghost inky = new Ghost("Inky");
	private final Ghost clyde = new Ghost("Clyde");

	@Override
	public void populate(World world) {
		all().forEach(creature -> creature.setWorld(world));
		world.setPopulation(this);
		defineBehavior(world);
		blinky.color = RED_GHOST;
		pinky.color = PINK_GHOST;
		inky.color = CYAN_GHOST;
		clyde.color = ORANGE_GHOST;
	}

	private void defineBehavior(World world) {
		int w = world.width(), h = world.height();
		pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		ghosts().forEach(ghost -> {
			ghost.behavior(LEAVING_HOUSE, ghost::leavingGhostHouse);
			ghost.behavior(FRIGHTENED, ghost.movingRandomly());
			ghost.behavior(DEAD, ghost.returningToHouse());
		});

		blinky.behavior(LOCKED, () -> blinky.bouncingOnBed(world.theHouse().bed(0)));
		blinky.behavior(ENTERING_HOUSE, blinky.goingToBed(world.theHouse().bed(2)));
		blinky.behavior(SCATTERING, blinky.headingFor(Tile.at(w - 3, 0)));
		blinky.behavior(CHASING, blinky.headingFor(pacMan::tile));

		inky.behavior(LOCKED, () -> inky.bouncingOnBed(world.theHouse().bed(1)));
		inky.behavior(ENTERING_HOUSE, inky.goingToBed(world.theHouse().bed(1)));
		inky.behavior(SCATTERING, inky.headingFor(Tile.at(w - 1, h - 1)));
		inky.behavior(CHASING, inky.headingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}));

		pinky.behavior(LOCKED, () -> pinky.bouncingOnBed(world.theHouse().bed(2)));
		pinky.behavior(ENTERING_HOUSE, pinky.goingToBed(world.theHouse().bed(2)));
		pinky.behavior(SCATTERING, pinky.headingFor(Tile.at(2, 0)));
		pinky.behavior(CHASING, pinky.headingFor(() -> pacMan.tilesAhead(4)));

		clyde.behavior(LOCKED, () -> clyde.bouncingOnBed(world.theHouse().bed(3)));
		clyde.behavior(ENTERING_HOUSE, clyde.goingToBed(world.theHouse().bed(3)));
		clyde.behavior(SCATTERING, clyde.headingFor(Tile.at(0, h - 1)));
		clyde.behavior(CHASING, clyde.headingFor(() -> clyde.distance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, h - 1)));
	}

	@Override
	public void takePartIn(Game game) {
		all().forEach(creature -> creature.takePartIn(game));
	}

	@Override
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

	@Override
	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	@Override
	public Stream<Creature<?>> all() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}
}