package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostName.Blinky;
import static de.amr.games.pacman.actor.GhostName.Clyde;
import static de.amr.games.pacman.actor.GhostName.Inky;
import static de.amr.games.pacman.actor.GhostName.Pinky;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SAFE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.view.Controller;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.theme.GhostColor;

/**
 * Factory and container for the game actors.
 * 
 * @author Armin Reichert
 */
public class Cast {

	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Actor> activeActors = new HashSet<>();

	public Cast(Game game) {
		Maze maze = game.getMaze();
		pacMan = new PacMan(game);
		blinky = new Ghost(Blinky, pacMan, game, maze.getBlinkyHome(), maze.getBlinkyScatteringTarget(),
				Top4.E, GhostColor.RED);
		pinky = new Ghost(Pinky, pacMan, game, maze.getPinkyHome(), maze.getPinkyScatteringTarget(),
				Top4.S, GhostColor.PINK);
		inky = new Ghost(Inky, pacMan, game, maze.getInkyHome(), maze.getInkyScatteringTarget(), Top4.N,
				GhostColor.TURQUOISE);
		clyde = new Ghost(Clyde, pacMan, game, maze.getClydeHome(), maze.getClydeScatteringTarget(),
				Top4.N, GhostColor.ORANGE);

		// Pac-Man behavior
		Navigation<PacMan> followKeyboard = pacMan.followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setMoveBehavior(PacManState.HUNGRY, followKeyboard);
		pacMan.setMoveBehavior(PacManState.GREEDY, followKeyboard);

		// common ghost behavior
		Stream.of(blinky, pinky, inky, clyde).forEach(ghost -> {
			ghost.setMoveBehavior(FRIGHTENED, ghost.flee(pacMan));
			ghost.setMoveBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
			ghost.setMoveBehavior(DEAD, ghost.headFor(ghost::getHome));
			ghost.setMoveBehavior(SAFE, ghost.bounce());
		});

		// individual ghost behavior
		blinky.setMoveBehavior(CHASING, blinky.attackDirectly(pacMan));
		pinky.setMoveBehavior(CHASING, pinky.ambush(pacMan, 4));
		inky.setMoveBehavior(CHASING, inky.attackWithPartner(blinky, pacMan));
		clyde.setMoveBehavior(CHASING, clyde.attackAndReject(clyde, pacMan, 8 * Game.TS));
		clyde.fnCanLeaveHouse = () -> game.getLevel() > 1
				|| game.getFoodRemaining() < (66 * maze.getFoodTotal() / 100);

		activeActors.addAll(Arrays.asList(pacMan, blinky, pinky, inky, clyde));
	}

	public void init() {
		activeActors.forEach(Controller::init);
	}

	public Ghost getBlinky() {
		return blinky;
	}

	public Ghost getPinky() {
		return pinky;
	}

	public Ghost getInky() {
		return inky;
	}

	public Ghost getClyde() {
		return clyde;
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public boolean isActive(Actor actor) {
		return activeActors.contains(actor);
	}

	public void setActive(Actor actor, boolean active) {
		if (active == isActive(actor)) {
			return;
		}
		if (active) {
			activeActors.add(actor);
			actor.init();
		} else {
			activeActors.remove(actor);
		}
	}

	public Stream<Ghost> getActiveGhosts() {
		return getGhosts().filter(this::isActive);
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public void traceTo(Logger logger) {
		pacMan.traceTo(logger);
		getGhosts().forEach(ghost -> ghost.traceTo(logger));
	}
}