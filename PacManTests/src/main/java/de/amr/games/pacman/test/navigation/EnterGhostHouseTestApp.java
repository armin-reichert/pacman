package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

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
		setController(new EnterGhostHouseTestUI(new Game(), new ArcadeTheme()));
	}
}

class EnterGhostHouseTestUI extends PlayView {

	public EnterGhostHouseTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		game.stage.add(game.inky);
		message.text = "SPACE = enter / leave house";
		message.fontSize = 9;
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