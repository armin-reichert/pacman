package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.Steerings.takingShortestPath;

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
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class TakeShortestPathTestUI extends PlayView implements VisualController {

	final Maze maze;
	final Ghost ghost;
	final List<Tile> targets;
	int currentTarget;

	public TakeShortestPathTestUI(PacManGameCast cast) {
		super(cast);
		cast.theme.snd_ghost_chase().volume(0);
		maze = cast.game.maze;
		ghost = cast.blinky;
		targets = Arrays.asList(maze.cornerSE, maze.tileAt(15, 23), maze.tileAt(12, 23), maze.cornerSW,
				maze.tunnelExitLeft, maze.cornerNW, maze.ghostHome[0], maze.cornerNE, maze.tunnelExitRight,
				maze.pacManHome);
	}

	@Override
	public void init() {
		super.init();
		game.init();
		maze.removeFood();
		currentTarget = 0;
		Steering<Ghost> shortestPath = takingShortestPath(maze, () -> targets.get(currentTarget));
		ghost.setSteering(CHASING, shortestPath);
		ghost.setSteering(FRIGHTENED, shortestPath);
		cast.activate(ghost);
		ghost.setState(CHASING);
		textColor = Color.YELLOW;
		message = "SPACE toggles ghost state";
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(true);
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
			ghost.setState(ghost.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		ghost.update();
		if (ghost.tile().equals(targets.get(currentTarget))) {
			nextTarget();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}