package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowMouseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;
	private Tile targetTile;

	public FollowMouseTestController(PacManTheme theme) {
		game = new PacManGame(theme);
		game.setLevel(1);
		game.maze.removeFood();
		view = new PlayViewXtended(game);
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
		targetTile = game.maze.getPacManHome();
		game.pacMan.placeAtTile(targetTile, 0, 0);
		game.ghosts().forEach(ghost -> game.setActive(ghost, false));
		game.setActive(game.blinky, true);
		game.setActive(game.pacMan, true);
		game.blinky.init();
		game.blinky.setState(GhostState.CHASING);
	}

	@Override
	public void update() {
		selectBehavior();
		readTargetTile();
		game.blinky.update();
		view.update();
	}

	private void readTargetTile() {
		if (Mouse.moved()) {
			targetTile = new Tile(Mouse.getX() / TS, Mouse.getY() / TS);
			game.pacMan.placeAtTile(targetTile, 0, 0);
			LOGGER.info(targetTile.toString());
		}
	}

	private void selectBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			game.blinky.setBehavior(GhostState.CHASING, game.blinky.attackDirectly(game.pacMan));
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			game.blinky.setBehavior(GhostState.CHASING, game.blinky.followRoute(() -> targetTile));
		}
	}
}