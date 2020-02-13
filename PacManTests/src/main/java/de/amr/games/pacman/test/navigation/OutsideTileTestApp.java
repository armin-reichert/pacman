package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class OutsideTileTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(OutsideTileTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Follow Tile Outside Maze";
	}

	@Override
	public void init() {
		Game game = new Game();
		Cast cast = new Cast(game);
		Theme theme = new ArcadeTheme();
		setController(new OutsideTileTestUI(cast, theme));
	}
}

class OutsideTileTestUI extends PlayView implements VisualController {

	public OutsideTileTestUI(Cast cast, Theme theme) {
		super(cast, theme);
		showRoutes = () -> true;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		theme.snd_ghost_chase().volume(0);
		cast.blinky.setActing(true);
		cast.blinky.behavior(CHASING, cast.blinky.isHeadingFor(() -> maze().tileAt(100, maze().portalRight.row)));
		cast.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		cast.blinky.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}