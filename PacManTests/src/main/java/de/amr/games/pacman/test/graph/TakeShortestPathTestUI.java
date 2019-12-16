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
	int current;

	public TakeShortestPathTestUI(PacManGameCast cast) {
		super(cast);
		cast.theme.snd_ghost_chase().volume(0);
		maze = cast.game.maze;
		ghost = cast.blinky;
		targets = Arrays.asList(maze.cornerSE, maze.tileAt(15, 23), maze.tileAt(12, 23), maze.cornerSW, maze.tunnelExitLeft,
				maze.cornerNW, maze.ghostHome[0], maze.cornerNE, maze.tunnelExitRight, maze.pacManHome);
	}

	Tile currentTarget() {
		return targets.get(current);
	}

	@Override
	public void init() {
		super.init();
		game.init();
		maze.removeFood();
		current = 0;
		ghost.fnChasingTarget = this::currentTarget;
		Steering<Ghost> shortestPath = takingShortestPath(maze, this::currentTarget);
		ghost.setSteering(CHASING, shortestPath);
		ghost.setSteering(FRIGHTENED, shortestPath);
		cast.putOnStage(ghost);
		ghost.setState(CHASING);
		textColor = Color.YELLOW;
		message = "SPACE toggles ghost state";
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(true);
	}

	private void nextTarget() {
		current += 1;
		if (current == targets.size()) {
			current = 0;
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
		if (ghost.tile().equals(targets.get(current))) {
			nextTarget();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}