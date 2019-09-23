package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowMouseTestController implements ViewController {

	private final PacManGame game;
	private final PacMan pacMan;
	private final Ghost blinky;
	private final PlayViewX view;
	private Tile targetTile;

	public FollowMouseTestController() {
		game = new PacManGame();
		pacMan = game.getPacMan();
		blinky = game.getBlinky();
		view = new PlayViewX(game);
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
		targetTile = game.getMaze().getPacManHome();
		pacMan.placeAtTile(targetTile, 0, 0);
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.getAllGhosts().forEach(ghost -> game.setActive(ghost, false));
		game.setActive(blinky, true);
		game.setActive(pacMan, true);
		blinky.init();
		blinky.setState(GhostState.CHASING);
		blinky.setBehavior(GhostState.CHASING, blinky.headFor(() -> targetTile));
	}

	@Override
	public void update() {
		selectBehavior();
		readTargetTile();
		blinky.update();
		view.update();
	}

	private void readTargetTile() {
		if (Mouse.moved()) {
			targetTile = new Tile(Mouse.getX() / TS, Mouse.getY() / TS);
			pacMan.placeAtTile(targetTile, 0, 0);
			LOGGER.info(targetTile.toString());
		}
	}

	private void selectBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			blinky.setBehavior(GhostState.CHASING, blinky.headFor(() -> targetTile));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			blinky.setBehavior(GhostState.CHASING, blinky.followRoute(() -> targetTile));
		}
	}
}