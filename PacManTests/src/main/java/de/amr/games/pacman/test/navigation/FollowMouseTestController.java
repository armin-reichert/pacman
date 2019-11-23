package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.model.PacManGame.TS;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowMouseTestController implements ViewController {

	private PacManGame g;
	private PlayViewXtended view;
	private Tile mouseTile;

	@Override
	public View currentView() {
		return view;
	}

	@Override
	public void init() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		g.setActive(g.pacMan, false);
		g.ghosts().forEach(ghost -> g.setActive(ghost, ghost == g.blinky));
		g.blinky.init();
		g.blinky.fnChasingTarget = () -> mouseTile;
		g.blinky.setState(CHASING);
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(false);
		view.setScoresVisible(false);
		Assets.muteAll(true);
		mouseTile = g.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
	}

	@Override
	public void update() {
		if (Mouse.moved()) {
			mouseTile = g.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
		}
		g.blinky.update();
		view.update();
	}
}