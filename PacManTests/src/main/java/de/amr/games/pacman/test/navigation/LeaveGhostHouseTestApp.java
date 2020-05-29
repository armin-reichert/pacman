package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class LeaveGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(LeaveGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Leave Ghost House";
	}

	@Override
	public void init() {
		clock().setTargetFramerate(10);
		setController(new LeaveGhostHouseTestUI(new Game(), new ArcadeTheme()));
	}
}

class LeaveGhostHouseTestUI extends PlayView {

	public LeaveGhostHouseTestUI(Game game, Theme theme) {
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
		game.inky.followState = SCATTERING;
		showMessage("Press SPACE to unlock", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE) && game.inky.is(GhostState.LOCKED)) {
			game.inky.process(new GhostUnlockedEvent());
			clearMessage();
		}
		game.inky.update();
		super.update();
	}
}