package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.model.PacManGame.TS;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class FollowMouseTestUI extends PlayView implements ViewController {

	private Tile mouseTile;

	public FollowMouseTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showGrid = true;
		showStates = false;
		showScores = false;
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.theme.snd_ghost_chase().volume(0);
		ensemble.blinky.activate();
		ensemble.blinky.init();
		ensemble.blinky.fnChasingTarget = () -> mouseTile;
		ensemble.blinky.setState(CHASING);
		mouseTile = game.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
	}

	@Override
	public void update() {
		if (Mouse.moved()) {
			mouseTile = game.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
		}
		ensemble.blinky.update();
		super.update();
	}
}