package de.amr.games.pacman.test.navigation;

import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class PacManMovementTestUI extends PlayViewXtended implements ViewController {

	public PacManMovementTestUI(PacManGame game) {
		super(game);
		setShowRoutes(false);
		setShowGrid(false);
		setShowStates(false);
		setScoresVisible(false);
	}

	@Override
	public void init() {
		game.level = 1;
		game.pacMan.addGameEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				game.theme.snd_eatPill().play();
				game.maze.removeFood(foodFound.tile);
				if (game.maze.tiles().filter(game.maze::containsFood).count() == 0) {
					game.maze.restoreFood();
				}
			}
		});
		game.ghosts().forEach(ghost -> game.setActive(ghost, false));
		game.pacMan.init();
	}

	@Override
	public void update() {
		game.activeActors().forEach(Actor::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}