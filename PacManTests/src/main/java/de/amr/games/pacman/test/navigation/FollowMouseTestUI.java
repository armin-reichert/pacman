package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steerings;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class FollowMouseTestUI extends PlayView implements VisualController {

	private Tile mouseTile;

	public FollowMouseTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(false);
		setShowScores(false);
		setShowGrid(true);
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		game.init();
		game.maze.removeFood();
		cast.theme.snd_ghost_chase().volume(0);
		cast.putOnStage(cast.blinky);
		cast.blinky.setSteering(CHASING, Steerings.headingForTargetTile(() -> mouseTile));
		cast.blinky.setState(CHASING);
		readMouse();
	}

	private void readMouse() {
		mouseTile = game.maze.tileAt(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
	}

	@Override
	public void update() {
		if (Mouse.moved()) {
			readMouse();
		}
		cast.blinky.update();
		super.update();
	}
}