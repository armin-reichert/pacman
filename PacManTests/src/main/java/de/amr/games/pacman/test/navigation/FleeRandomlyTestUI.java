package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class FleeRandomlyTestUI extends PlayView implements VisualController {

	public FleeRandomlyTestUI(PacManGame game, PacManGameCast ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.start();
		game.maze.removeFood();
		cast.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
			ghost.setState(FRIGHTENED);
		});
		cast.blinky.placeAtTile(game.maze.topLeft, 0, 0);
		cast.pinky.placeAtTile(game.maze.topRight, 0, 0);
		cast.inky.placeAtTile(game.maze.bottomLeft, 0, 0);
		cast.clyde.placeAtTile(game.maze.bottomRight, 0, 0);
	}

	@Override
	public void update() {
		cast.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}