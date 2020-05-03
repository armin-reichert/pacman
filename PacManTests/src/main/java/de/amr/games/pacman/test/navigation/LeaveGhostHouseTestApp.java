package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class LeaveGhostHouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(LeaveGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Leave Ghost House";
	}

	@Override
	public void init() {
		clock().setTargetFramerate(10);
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new LeaveGhostHouseTestUI(game, theme));
	}
}

class LeaveGhostHouseTestUI extends PlayView implements VisualController {

	public LeaveGhostHouseTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.tiles().forEach(game.maze::removeFood);
		game.stage.add(game.inky);
		game.inky.followState = SCATTERING;
		message.text = "Press SPACE to unlock";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE) && game.inky.is(GhostState.LOCKED)) {
			game.inky.process(new GhostUnlockedEvent());
			message.text = "";
		}
		game.inky.update();
		super.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}