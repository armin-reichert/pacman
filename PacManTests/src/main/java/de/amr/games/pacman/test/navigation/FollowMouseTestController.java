package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowMouseTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public FollowMouseTestController(PacManTheme theme) {
		g = new PacManGame(theme);
		g.setLevel(1);
		g.maze.removeFood();
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public View currentView() {
		return view;
	}

	@Override
	public void init() {
		g.pacMan.placeAtTile(g.maze.getPacManHome(), 0, 0);
		g.ghosts().forEach(ghost -> g.setActive(ghost, false));
		g.setActive(g.blinky, true);
		g.setActive(g.pacMan, true);
		g.blinky.init();
		g.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		handleRoutingChange();
		handleTargetChange();
		g.blinky.update();
		view.update();
	}

	private void handleTargetChange() {
		if (Mouse.moved()) {
			Tile mousePosition = new Tile(Mouse.getX() / TS, Mouse.getY() / TS);
			g.pacMan.placeAtTile(mousePosition, 0, 0);
			LOGGER.info("New position of Pac-Man: " + mousePosition.toString());
		}
	}

	private void handleRoutingChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			g.blinky.setBehavior(CHASING, g.blinky.attackDirectly(g.pacMan));
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			g.blinky.setBehavior(CHASING, g.blinky.followRoute(() -> g.pacMan.getTile()));
		}
	}
}