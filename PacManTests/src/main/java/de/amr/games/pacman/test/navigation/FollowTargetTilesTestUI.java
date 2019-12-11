package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class FollowTargetTilesTestUI extends PlayView implements VisualController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes = true;
		showStates = false;
		showScores = false;
		targets = Arrays.asList(game.maze.cornerNW, game.maze.ghostHome[0], game.maze.cornerNE,
				game.maze.cornerSE, game.maze.pacManHome, game.maze.cornerSW);
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		game.newGame();
		game.maze.removeFood();
		cast.theme.snd_ghost_chase().volume(0);
		cast.activate(cast.blinky);
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
				game.nextLevel();
			}
		}
		cast.blinky.update();
		super.update();
	}
}