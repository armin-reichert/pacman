package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.behavior.Steerings.enteringGhostHouse;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;

public class EnterGhostHouseTestUI extends PlayView implements VisualController {

	final Maze maze;
	final Ghost inky;

	public EnterGhostHouseTestUI(PacManGameCast cast) {
		super(cast);
		maze = cast.game.maze;
		inky = cast.inky;
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
		cast.putOnStage(inky);
		inky.placeAtTile(maze.ghostHome[0], Tile.SIZE / 2, 0);
		inky.setSteering(ENTERING_HOUSE, enteringGhostHouse(maze, maze.ghostHome[1]));
		textColor = Color.YELLOW;
		message = "Press SPACE to enter or leave house";
	}

	@Override
	public void update() {
		boolean outside = !maze.partOfGhostHouse(inky.tile());
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			if (inky.is(LOCKED)) {
				inky.setState(outside ? ENTERING_HOUSE : LEAVING_HOUSE);
			} else if (inky.is(LEAVING_HOUSE) && maze.inFrontOfGhostHouseDoor(inky.tile())) {
				inky.setState(LOCKED);
			}
		} else {
			inky.update();
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}