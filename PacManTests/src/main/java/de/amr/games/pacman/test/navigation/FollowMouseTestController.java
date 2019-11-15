package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.model.PacManGame.TS;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowMouseTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;
	private Tile currentMouseTile;

	public FollowMouseTestController() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		g.setActive(g.pacMan, false);
		g.ghosts().forEach(ghost -> g.setActive(ghost, ghost == g.blinky));
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(false);
		view.setScoresVisible(false);
		currentMouseTile = g.maze.tileAt(0, 0);
	}

	@Override
	public View currentView() {
		return view;
	}

	@Override
	public void init() {
		g.blinky.init();
		g.blinky.setBehavior(CHASING, g.blinky.headingFor(() -> currentMouseTile));
		g.blinky.setState(CHASING);
		g.theme.snd_ghost_chase().stop();
	}

	@Override
	public void update() {
		handleMouseMove();
		g.blinky.update();
		view.update();
	}

	private void handleMouseMove() {
		if (Mouse.moved()) {
			currentMouseTile = g.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
			LOGGER.info("New mouse position: " + currentMouseTile.toString());
		}
	}
}