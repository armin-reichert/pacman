package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Maze;
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
		game().init();
		maze().removeFood();
		cast().putOnStage(inky);
		messageColor = Color.YELLOW;
		messageText = "SPACE = enter / leave house";
	}

	@Override
	public void update() {
		boolean outside = !maze.partOfGhostHouse(inky.tile());
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			inky.setState(outside ? ENTERING_HOUSE : LEAVING_HOUSE);
		}
		inky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}