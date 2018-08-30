package de.amr.games.pacman.actor;

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
import java.util.stream.Stream;

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

	public static final String PACMAN = "Pac-Man";
	public static final String BLINKY = "Blinky";
	public static final String PINKY = "Pinky";
	public static final String INKY = "Inky";
	public static final String CLYDE = "Clyde";

	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	private final Set<Actor> activeActors = new HashSet<>();

	public Cast(Game game) {
		Maze maze = game.getMaze();

		// Pac-Man
		pacMan = new PacMan(game);

		// The ghosts
		blinky = new Ghost(BLINKY, pacMan, game, maze.getBlinkyHome(), maze.getBlinkyScatteringTarget(),
				Top4.E, GhostColor.RED);

		pinky = new Ghost(PINKY, pacMan, game, maze.getPinkyHome(), maze.getPinkyScatteringTarget(),
				Top4.S, GhostColor.PINK);

		inky = new Ghost(INKY, pacMan, game, maze.getInkyHome(), maze.getInkyScatteringTarget(), Top4.N,
				GhostColor.TURQUOISE);

		clyde = new Ghost(CLYDE, pacMan, game, maze.getClydeHome(), maze.getClydeScatteringTarget(),
				Top4.N, GhostColor.ORANGE);

		activeActors.addAll(Arrays.asList(pacMan, blinky, pinky, inky, clyde));

		// Define the navigation behavior ("AI")

		// Pac-Man is controlled by the keyboard
		Navigation<PacMan> followKeyboard = pacMan.followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setMoveBehavior(PacManState.HUNGRY, followKeyboard);
		pacMan.setMoveBehavior(PacManState.GREEDY, followKeyboard);

		// Common ghost behavior
		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(FRIGHTENED, ghost.flee(pacMan));
			ghost.setMoveBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
			ghost.setMoveBehavior(DEAD, ghost.headFor(ghost::getHome));
			ghost.setMoveBehavior(SAFE, ghost.bounce());
		});

		// Individual ghost behavior
		blinky.setMoveBehavior(CHASING, blinky.attackDirectly(pacMan));
		pinky.setMoveBehavior(CHASING, pinky.ambush(pacMan, 4));
		inky.setMoveBehavior(CHASING, inky.attackWithPartner(blinky, pacMan));
		clyde.setMoveBehavior(CHASING, clyde.attackAndReject(clyde, pacMan, 8 * Game.TS));

		// Other game rules
		clyde.fnCanLeaveHouse = () -> game.getLevel() > 1
				|| game.getFoodRemaining() < (66 * maze.getFoodTotal() / 100);
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> getActiveGhosts() {
		return getGhosts().filter(this::isActive);
	}

	public void init() {
		activeActors.forEach(Actor::init);
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
		actor.setVisible(active);
	}
}