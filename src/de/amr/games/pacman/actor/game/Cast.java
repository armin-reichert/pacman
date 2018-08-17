package de.amr.games.pacman.actor.game;

import static de.amr.games.pacman.navigation.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.chase;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.flee;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.navigation.impl.NavigationSystem.go;
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
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.view.PacManSprites.GhostColor;

/**
 * Factory and container for the game actors.
 * 
 * @author Armin Reichert
 */
public class Cast implements PacManWorld {

	private static PacMan createPacMan(Game game, PacManWorld world) {
		PacMan pacMan = new PacMan(game, world);
		Navigation keySteering = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setNavigation(PacManState.HUNGRY, keySteering);
		pacMan.setNavigation(PacManState.GREEDY, keySteering);
		return pacMan;
	}

	private static Ghost createBlinky(Game game, PacMan pacMan, Tile home) {
		Ghost ghost = new Ghost(GhostName.Blinky, pacMan, game, home, Top4.E, GhostColor.RED);
		ghost.setNavigation(GhostState.AGGRO, chase(pacMan));
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, go(home));
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createPinky(Game game, PacMan pacMan, Tile home) {
		Ghost ghost = new Ghost(GhostName.Pinky, pacMan, game, home, Top4.S, GhostColor.PINK);
		ghost.setNavigation(GhostState.AGGRO, ambush(pacMan));
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, go(home));
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createInky(Game game, PacMan pacMan, Tile home) {
		Ghost ghost = new Ghost(GhostName.Inky, pacMan, game, home, Top4.N, GhostColor.TURQUOISE);
		ghost.setNavigation(GhostState.AGGRO, flee(pacMan)); // TODO
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, go(home));
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createClyde(Game game, PacMan pacMan, Tile home) {
		Ghost ghost = new Ghost(GhostName.Clyde, pacMan, game, home, Top4.N, GhostColor.ORANGE);
		ghost.setNavigation(GhostState.AGGRO, flee(pacMan)); // TODO
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, go(home));
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Ghost> activeGhosts = new HashSet<>(4);
	private Bonus bonus;

	public Cast(Game game) {
		pacMan = createPacMan(game, this);
		blinky = createBlinky(game, pacMan, game.maze.getBlinkyHome());
		pinky = createPinky(game, pacMan, game.maze.getPinkyHome());
		inky = createInky(game, pacMan, game.maze.getInkyHome());
		clyde = createClyde(game, pacMan, game.maze.getClydeHome());
		activeGhosts.addAll(Arrays.asList(blinky, pinky, inky, clyde));
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