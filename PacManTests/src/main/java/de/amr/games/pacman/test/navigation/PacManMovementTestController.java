package de.amr.games.pacman.test.navigation;

import java.awt.Color;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class PacManMovementTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public PacManMovementTestController(PacManTheme theme) {
		game = new PacManGame(theme);
		view = new PlayViewXtended(game, Color.BLACK);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		// game.maze.tiles().filter(game.maze::isFood).forEach(game::eatFoodAtTile);
		game.pacMan.getEventManager().addListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				game.theme.snd_eatPill().play();
				game.maze.removeFood(foodFound.tile);
			}
		});
		game.ghosts().forEach(ghost -> game.setActive(ghost, false));
		game.setActive(game.pacMan, true);
		game.pacMan.init();
	}

	@Override
	public void update() {
		game.pacMan.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}