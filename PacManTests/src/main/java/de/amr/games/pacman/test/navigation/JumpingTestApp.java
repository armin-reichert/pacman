package de.amr.games.pacman.test.navigation;

import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class JumpingTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(JumpingTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Jumping";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new JumpingTestUI(game, theme));
	}
}

class JumpingTestUI extends PlayView implements VisualController {

	public JumpingTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = () -> false;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.tiles().forEach(game.maze::removeFood);
		game.ghosts().forEach(game.stage::add);
	}

	@Override
	public void update() {
		super.update();
		game.ghostsOnStage().forEach(Ghost::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}