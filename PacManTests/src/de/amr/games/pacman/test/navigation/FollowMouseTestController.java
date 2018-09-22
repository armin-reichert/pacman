package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowMouseTestController implements ViewController {

	private final Game game;
	private final PlayViewX view;
	private final Cast actors;
	private Tile mouseTile;

	public FollowMouseTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze);
		actors = new Cast(game);
		view = new PlayViewX(game, actors);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		mouseTile = actors.pacMan.getHomeTile();
		actors.pacMan.placeAtTile(mouseTile, 0, 0);
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		actors.setActive(actors.blinky, true);
		actors.setActive(actors.pinky, false);
		actors.setActive(actors.inky, false);
		actors.setActive(actors.clyde, false);
		actors.setActive(actors.pacMan, true);
		actors.blinky.init();
		actors.blinky.setState(GhostState.CHASING);
		actors.blinky.setMoveBehavior(GhostState.CHASING, actors.blinky.followDynamicRoute(() -> mouseTile));
	}

	@Override
	public void update() {
		updateMouseTile();
		actors.blinky.update();
		view.update();
	}

	private void updateMouseTile() {
		if (Mouse.moved()) {
			int x = Mouse.getX(), y = Mouse.getY();
			mouseTile = new Tile(x / Game.TS, y / Game.TS);
			actors.pacMan.placeAtTile(mouseTile, 0, 0);
			Application.LOGGER.info(mouseTile.toString());
		}
	}

	@Override
	public View currentView() {
		return view;
	}
}