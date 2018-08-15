package de.amr.games.pacman.actor.game;

import static de.amr.games.pacman.routing.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.routing.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.routing.impl.NavigationSystem.chase;
import static de.amr.games.pacman.routing.impl.NavigationSystem.flee;
import static de.amr.games.pacman.routing.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.routing.impl.NavigationSystem.goHome;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.EventManager;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.view.PacManSprites.GhostColor;

/**
 * Factory and container for the game actors.
 * 
 * @author Armin Reichert
 */
public class Cast implements PacManWorld {

	private static PacMan createPacMan(Game game, EventManager<GameEvent> events) {
		PacMan pacMan = new PacMan(game);
		Navigation keySteering = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setNavigation(PacManState.HUNGRY, keySteering);
		pacMan.setNavigation(PacManState.GREEDY, keySteering);
		pacMan.setEventManager(events);
		return pacMan;
	}

	private static Ghost createBlinky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(GhostName.Blinky, pacMan, game, game.maze.blinkyHome, Top4.E, GhostColor.RED);
		ghost.setNavigation(GhostState.AGGRO, chase(pacMan));
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, goHome());
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createPinky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(GhostName.Pinky, pacMan, game, game.maze.pinkyHome, Top4.S, GhostColor.PINK);
		ghost.setNavigation(GhostState.AGGRO, ambush(pacMan));
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, goHome());
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createInky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(GhostName.Inky, pacMan, game, game.maze.inkyHome, Top4.N, GhostColor.TURQUOISE);
		ghost.setNavigation(GhostState.AGGRO, ambush(pacMan)); // TODO
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, goHome());
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private static Ghost createClyde(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(GhostName.Clyde, pacMan, game, game.maze.clydeHome, Top4.N, GhostColor.ORANGE);
		ghost.setNavigation(GhostState.AGGRO, ambush(pacMan)); // TODO
		ghost.setNavigation(GhostState.AFRAID, flee(pacMan));
		ghost.setNavigation(GhostState.DEAD, goHome());
		ghost.setNavigation(GhostState.SAFE, bounce());
		return ghost;
	}

	private final EventManager<GameEvent> events;
	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Ghost> activeGhosts = new HashSet<>(4);
	private Bonus bonus;

	public Cast(Game game) {
		events = new EventManager<>("[ActorEvents]");
		pacMan = createPacMan(game, events);
		pacMan.setWorld(this);
		blinky = createBlinky(game, pacMan);
		pinky = createPinky(game, pacMan);
		inky = createInky(game, pacMan);
		clyde = createClyde(game, pacMan);
		activeGhosts.addAll(Arrays.asList(blinky, pinky, inky, clyde));
	}

	public void init() {
		pacMan.init();
		activeGhosts.forEach(Ghost::init);
		removeBonus();
	}

	public void subscribe(Consumer<GameEvent> observer) {
		events.subscribe(observer);
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