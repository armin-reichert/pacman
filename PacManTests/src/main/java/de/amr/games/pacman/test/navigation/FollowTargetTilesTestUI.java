package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowTargetTilesTestUI extends PlayViewXtended implements ViewController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = false;
		scoresVisible = false;
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		targets = Arrays.asList(game.maze.topLeft, game.maze.blinkyHome, game.maze.topRight,
				game.maze.bottomRight, game.maze.pacManHome, game.maze.bottomLeft);
		current = 0;

		game.level = 1;
		game.maze.removeFood();
		game.setActive(game.pacMan, false);
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.init();
		game.blinky.fnChasingTarget = () -> targets.get(current);
		game.blinky.placeAtTile(targets.get(0), 0, 0);
		game.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		if (game.blinky.currentTile() == targets.get(current)) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.level += 1;
			}
		}
		game.blinky.update();
		super.update();
	}
}