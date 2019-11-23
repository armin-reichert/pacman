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

public class FollowMouseTestUI extends PlayViewXtended implements ViewController {

	private Tile mouseTile;

	public FollowMouseTestUI(PacManGame game) {
		super(game);
		setShowRoutes(true);
		setShowGrid(true);
		setShowStates(false);
		setScoresVisible(false);
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		game.level = 1;
		game.maze.removeFood();
		game.setActive(game.pacMan, false);
		game.ghosts().forEach(ghost -> game.setActive(ghost, ghost == game.blinky));
		game.blinky.init();
		game.blinky.fnChasingTarget = () -> mouseTile;
		game.blinky.setState(CHASING);
		Assets.muteAll(true);
		mouseTile = game.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
	}

	@Override
	public void update() {
		if (Mouse.moved()) {
			mouseTile = game.maze.tileAt(Mouse.getX() / TS, Mouse.getY() / TS);
		}
		game.blinky.update();
		super.update();
	}
}