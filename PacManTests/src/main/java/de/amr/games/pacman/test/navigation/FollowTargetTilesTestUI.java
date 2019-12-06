package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class FollowTargetTilesTestUI extends PlayView implements VisualController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(PacManGame game, PacManGameCast ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = false;
		showScores = false;
		targets = Arrays.asList(game.maze.topLeft, game.maze.blinkyHome, game.maze.topRight,
				game.maze.bottomRight, game.maze.pacManHome, game.maze.bottomLeft);
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		game.start();
		game.maze.removeFood();
		cast.theme.snd_ghost_chase().volume(0);
		cast.blinky.activate();
		cast.blinky.fnChasingTarget = () -> targets.get(current);
		cast.blinky.placeAtTile(targets.get(0), 0, 0);
		cast.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		if (cast.blinky.tile() == targets.get(current)) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.levelNumber += 1;
			}
		}
		cast.blinky.update();
		super.update();
	}
}