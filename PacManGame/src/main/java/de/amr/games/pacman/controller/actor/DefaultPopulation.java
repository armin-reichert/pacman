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

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Population;
import de.amr.games.pacman.model.world.Tile;

public class DefaultPopulation implements Population {

	private final PacMan pacMan = new PacMan();
	private final Ghost blinky = new Ghost("Blinky");
	private final Ghost pinky = new Ghost("Pinky");
	private final Ghost inky = new Ghost("Inky");
	private final Ghost clyde = new Ghost("Clyde");

	@Override
	public void populate(PacManWorld world) {
		creatures().forEach(creature -> creature.setWorld(world));
		world.accept(this);
		pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		defineGhostBehavior(world);
	}

	private void defineGhostBehavior(PacManWorld world) {
		ghosts().forEach(ghost -> {
			ghost.behavior(LOCKED, ghost::bouncingOnBed);
			ghost.behavior(ENTERING_HOUSE, ghost.isGoingToBed(ghost.bed()));
			ghost.behavior(LEAVING_HOUSE, ghost::leavingGhostHouse);
			ghost.behavior(FRIGHTENED, ghost.movingRandomly());
			ghost.behavior(DEAD, ghost.isReturningToHouse());
		});
		blinky.behavior(ENTERING_HOUSE, blinky.isGoingToBed(world.theHouse().bed(2)));
		int w = world.width(), h = world.height();
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

	@Override
	public void play(Game game) {
		creatures().forEach(creature -> creature.game = game);
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
	public Ghost inky() {
		return inky;
	}

	@Override
	public Ghost pinky() {
		return pinky;
	}

	@Override
	public Ghost clyde() {
		return clyde;
	}

	@Override
	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	@Override
	public Stream<Creature<?>> creatures() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}
}