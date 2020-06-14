package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class EnterGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(EnterGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Enter Ghost House";
	}

	@Override
	public void init() {
		setController(new EnterGhostHouseTestUI());
	}
}

class EnterGhostHouseTestUI extends PlayView {

	public EnterGhostHouseTestUI() {
		super(new Game(), new ArcadeTheme());
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.eatAllFood();
		game.putOnStage(game.inky);
		showMessage("SPACE = Enter/leave house", Color.WHITE, 8);
	}

	@Override
	public void update() {
		super.update();
		boolean outside = !game.maze.insideGhostHouse(game.inky.tile());
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.inky.setState(outside ? ENTERING_HOUSE : LEAVING_HOUSE);
		}
		game.inky.update();
	}
}