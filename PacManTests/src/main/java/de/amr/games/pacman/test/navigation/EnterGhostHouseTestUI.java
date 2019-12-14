package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steerings;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class EnterGhostHouseTestUI extends PlayView implements VisualController {

	final Maze maze;
	final Ghost ghost;

	public EnterGhostHouseTestUI(PacManGameCast cast) {
		super(cast);
		maze = cast.game.maze;
		ghost = cast.inky;
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(true);
	}

	@Override
	public void init() {
		super.init();
		game.init();
		game.maze.removeFood();
		cast.activate(ghost);
		ghost.init();
		ghost.placeAtTile(maze.ghostHome[0], Tile.SIZE / 2, 0);
		ghost.setSteering(GhostState.ENTERING_HOUSE, Steerings.enteringGhostHouse(maze, maze.ghostHome[1]));
		textColor = Color.YELLOW;
		message = "Press SPACE to enter or leave house";
	}

	@Override
	public void update() {
		boolean outside = !maze.partOfGhostHouse(ghost.tile());
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			if (ghost.getState() == GhostState.LOCKED) {
				ghost.setState(outside ? GhostState.ENTERING_HOUSE : GhostState.LEAVING_HOUSE);
			}
			else if (ghost.getState() == GhostState.LEAVING_HOUSE && maze.inFrontOfGhostHouseDoor(ghost.tile())) {
				ghost.setState(GhostState.LOCKED);
			}
		}
		else {
			ghost.update();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}