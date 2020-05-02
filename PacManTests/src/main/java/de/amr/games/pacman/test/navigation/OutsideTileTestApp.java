package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
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
		Theme theme = new ArcadeTheme();
		setController(new OutsideTileTestUI(game, theme));
	}
}

class OutsideTileTestUI extends PlayView implements VisualController {

	public OutsideTileTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = () -> true;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		theme.snd_ghost_chase().volume(0);
		game.stage.add(game.blinky);
		game.blinky.behavior(CHASING, game.blinky.isHeadingFor(() -> game.maze.tileAt(100, game.maze.portalRight.row)));
		game.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		game.blinky.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}