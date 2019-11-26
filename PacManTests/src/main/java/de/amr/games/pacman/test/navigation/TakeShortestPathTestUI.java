package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.followingFixedPath;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class TakeShortestPathTestUI extends PlayViewXtended implements ViewController {

	private List<Tile> targets;
	private int targetIndex;

	public TakeShortestPathTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = true;
		scoresVisible = false;
	}

	@Override
	public void init() {
		super.init();
		targets = Arrays.asList(game.maze.bottomRight, game.maze.bottomLeft, game.maze.tunnelLeftExit,
				game.maze.topLeft, game.maze.blinkyHome, game.maze.topRight, game.maze.tunnelRightExit,
				game.maze.pacManHome);
		targetIndex = 0;
		game.level = 1;
		game.maze.removeFood();
		game.setActive(game.pacMan, false);
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.init();
		Steering<Ghost> followPathToCurrentTarget = followingFixedPath(() -> targets.get(targetIndex));
		game.blinky.setState(CHASING);
		game.blinky.setSteering(CHASING, followPathToCurrentTarget);
		game.blinky.setSteering(FRIGHTENED, followPathToCurrentTarget);
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			game.level += 1;
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			game.blinky.setState(game.blinky.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		game.blinky.update();
		if (game.blinky.currentTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}