package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class EnterGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(new EnterGhostHouseTestApp(), args);
	}

	public EnterGhostHouseTestApp() {
		settings.title = "Enter Ghost House";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new EnterGhostHouseTestUI(cast));
	}
}

class EnterGhostHouseTestUI extends PlayView implements VisualController {

	public EnterGhostHouseTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(true);
		showStates(true);
		showScores(false);
		showGrid(true);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().setActorOnStage(cast().inky);
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