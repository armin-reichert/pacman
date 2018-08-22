package de.amr.games.pacman.actor.game;

import static de.amr.games.pacman.actor.game.GhostName.Blinky;
import static de.amr.games.pacman.actor.game.GhostName.Clyde;
import static de.amr.games.pacman.actor.game.GhostName.Inky;
import static de.amr.games.pacman.actor.game.GhostName.Pinky;
import static de.amr.games.pacman.actor.game.GhostState.AGGRO;
import static de.amr.games.pacman.actor.game.GhostState.DEAD;
import static de.amr.games.pacman.actor.game.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.game.GhostState.SAFE;
import static de.amr.games.pacman.actor.game.GhostState.SCATTERING;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.chase;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.flee;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.go;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.scatter;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.navigation.impl.NavigationSystem;
import de.amr.games.pacman.view.PacManSprites.GhostColor;

/**
 * Factory and container for the game actors.
 * 
 * @author Armin Reichert
 */
public class Cast implements PacManWorld {

	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Ghost> activeGhosts = new HashSet<>(4);
	private Bonus bonus;

	public Cast(Game game) {
		Maze maze = game.getMaze();
		pacMan = new PacMan(game, this);
		blinky = new Ghost(Blinky, pacMan, game, maze.getBlinkyHome(), Top4.E, GhostColor.RED);
		pinky = new Ghost(Pinky, pacMan, game, maze.getPinkyHome(), Top4.S, GhostColor.PINK);
		inky = new Ghost(Inky, pacMan, game, maze.getInkyHome(), Top4.N, GhostColor.TURQUOISE);
		clyde = new Ghost(Clyde, pacMan, game, maze.getClydeHome(), Top4.N, GhostColor.ORANGE);
		configurePacMan();
		configureBlinky(maze);
		configurePinky(maze);
		configureInky(maze);
		configureClyde(maze);
		activeGhosts.addAll(Arrays.asList(blinky, pinky, inky, clyde));
	}

	private void configurePacMan() {
		Navigation keySteering = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setNavigation(PacManState.HUNGRY, keySteering);
		pacMan.setNavigation(PacManState.GREEDY, keySteering);
	}

	private void configureBlinky(Maze maze) {
		blinky.setNavigation(AGGRO, chase(pacMan));
		blinky.setNavigation(FRIGHTENED, flee(pacMan));
		blinky.setNavigation(SCATTERING, scatter(maze, maze.getBlinkyScatteringTarget()));
		blinky.setNavigation(DEAD, go(blinky.getHome()));
		blinky.setNavigation(SAFE, bounce());
	}

	private void configurePinky(Maze maze) {
		pinky.setNavigation(AGGRO, ambush(pacMan));
		pinky.setNavigation(FRIGHTENED, flee(pacMan));
		pinky.setNavigation(SCATTERING, scatter(maze, maze.getPinkyScatteringTarget()));
		pinky.setNavigation(DEAD, go(pinky.getHome()));
		pinky.setNavigation(SAFE, bounce());
	}

	private void configureInky(Maze maze) {
		inky.setNavigation(AGGRO, NavigationSystem.moody(blinky, pacMan));
		inky.setNavigation(FRIGHTENED, flee(pacMan));
		inky.setNavigation(SCATTERING, scatter(maze, maze.getInkyScatteringTarget()));
		inky.setNavigation(DEAD, go(inky.getHome()));
		inky.setNavigation(SAFE, bounce());
	}

	private void configureClyde(Maze maze) {
		clyde.setNavigation(AGGRO, flee(pacMan)); // TODO
		clyde.setNavigation(FRIGHTENED, flee(pacMan));
		clyde.setNavigation(SCATTERING, scatter(maze, maze.getClydeScatteringTarget()));
		clyde.setNavigation(DEAD, go(clyde.getHome()));
		clyde.setNavigation(SAFE, bounce());
	}

	public void init() {
		pacMan.init();
		activeGhosts.forEach(Ghost::init);
		removeBonus();
	}

	@Override
	public Ghost getBlinky() {
		return blinky;
	}

	@Override
	public Ghost getPinky() {
		return pinky;
	}

	@Override
	public Ghost getInky() {
		return inky;
	}

	@Override
	public Ghost getClyde() {
		return clyde;
	}

	@Override
	public PacMan getPacMan() {
		return pacMan;
	}

	public boolean isActive(Ghost ghost) {
		return activeGhosts.contains(ghost);
	}

	public void setActive(Ghost ghost, boolean active) {
		if (active == isActive(ghost)) {
			return;
		}
		if (active) {
			activeGhosts.add(ghost);
			ghost.init();
		} else {
			activeGhosts.remove(ghost);
		}
	}

	@Override
	public Stream<Ghost> getActiveGhosts() {
		return activeGhosts.stream();
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public void addBonus(BonusSymbol symbol, int value) {
		bonus = new Bonus(symbol, value);
	}

	public void removeBonus() {
		bonus = null;
	}

	@Override
	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}
}