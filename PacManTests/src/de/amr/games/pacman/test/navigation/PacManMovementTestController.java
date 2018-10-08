package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.PacManActors;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewX;

public class PacManMovementTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final PacManActors actors;

	public PacManMovementTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new PacManGame(maze);
		actors = new PacManActors(game);
		view = new PlayViewX(game, actors);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	private PacManTheme getTheme() {
		return Application.app().settings.get("theme");
	}

	@Override
	public void init() {
		game.setLevel(1);
		// game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		actors.pacMan.getEventManager().subscribe(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				getTheme().snd_eatPill().play();
				game.getMaze().hideFood(foodFound.tile);
			}
		});
		actors.setActive(actors.blinky, false);
		actors.setActive(actors.pinky, false);
		actors.setActive(actors.inky, false);
		actors.setActive(actors.clyde, false);
		actors.setActive(actors.pacMan, true);
		actors.pacMan.init();
	}

	@Override
	public void update() {
		actors.pacMan.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}