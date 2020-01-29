package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class EnterGhostHouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(EnterGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.title = "Enter Ghost House";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new EnterGhostHouseTestUI(cast));
	}
}

class EnterGhostHouseTestUI extends PlayView implements VisualController {

	public EnterGhostHouseTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		cast().putActorOnStage(cast().inky);
		messageColor = Color.YELLOW;
		messageText = "SPACE = enter / leave house";
	}

	@Override
	public void update() {
		super.update();
		boolean outside = !maze().partOfGhostHouse(cast.inky.tile());
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast.inky.setState(outside ? ENTERING_HOUSE : LEAVING_HOUSE);
		}
		cast.inky.update();
	}
}