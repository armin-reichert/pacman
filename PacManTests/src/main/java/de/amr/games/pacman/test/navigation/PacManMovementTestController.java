package de.amr.games.pacman.test.navigation;

import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class PacManMovementTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public PacManMovementTestController() {
		g = new PacManGame();
		view = new PlayViewXtended(g);
		view.setShowRoutes(false);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		Application.LOGGER.setLevel(Level.FINE);
		g.level = 1;
		// game.maze.tiles().filter(game.maze::isFood).forEach(game::eatFoodAtTile);
		g.pacMan.addGameEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				g.theme.snd_eatPill().play();
				g.maze.removeFood(foodFound.tile);
				if (g.maze.tiles().filter(g.maze::containsFood).count() == 0) {
					g.maze.restoreFood();
				}
			}
		});
		g.setActive(g.blinky, false);
		g.setActive(g.pacMan, true);
		g.activeActors().forEach(Entity::init);
	}

	@Override
	public void update() {
		g.activeActors().forEach(MazeMover::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}