package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.common.Steerings.followingShortestPath;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class TakeShortestPathTestUI extends PlayView implements VisualController {

	private List<Tile> targets;
	private int currentTarget;

	public TakeShortestPathTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes = true;
		showStates = true;
		showScores = false;
		targets = Arrays.asList(game.maze.cornerSE, game.maze.cornerSW, game.maze.tunnelExitLeft, game.maze.cornerNW,
				game.maze.ghostHome[0], game.maze.cornerNE, game.maze.tunnelExitRight, game.maze.pacManHome);
	}

	@Override
	public void init() {
		super.init();
		textColor = Color.YELLOW;
		message = "SPACE toggles ghost state";
		currentTarget = 0;
		game.init();
		game.maze.removeFood();
		cast.theme.snd_ghost_chase().volume(0);
		cast.activate(cast.blinky);
		cast.blinky.init();
		cast.blinky.setState(CHASING);
		Steering<Ghost> followPathToCurrentTarget = followingShortestPath(() -> targets.get(currentTarget));
		cast.blinky.setSteering(CHASING, followPathToCurrentTarget);
		cast.blinky.setSteering(FRIGHTENED, followPathToCurrentTarget);
	}

	private void nextTarget() {
		currentTarget += 1;
		if (currentTarget == targets.size()) {
			currentTarget = 0;
			game.enterLevel(game.level.number + 1);
			game.maze.removeFood();
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast.blinky.setState(cast.blinky.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		cast.blinky.update();
		if (cast.blinky.tile().equals(targets.get(currentTarget))) {
			nextTarget();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}