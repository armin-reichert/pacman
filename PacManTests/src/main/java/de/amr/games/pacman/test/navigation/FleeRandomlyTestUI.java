package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class FleeRandomlyTestUI extends PlayView implements ViewController {

	public FleeRandomlyTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
			ghost.setState(FRIGHTENED);
		});
		ensemble.blinky.placeAtTile(game.maze.topLeft, 0, 0);
		ensemble.pinky.placeAtTile(game.maze.topRight, 0, 0);
		ensemble.inky.placeAtTile(game.maze.bottomLeft, 0, 0);
		ensemble.clyde.placeAtTile(game.maze.bottomRight, 0, 0);
	}

	@Override
	public void update() {
		ensemble.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}