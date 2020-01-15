package de.amr.games.pacman.test.navigation;

import java.util.Optional;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class JumpingTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new JumpingTestApp(), args);
	}

	public JumpingTestApp() {
		settings().title = "Jumping";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new JumpingTestUI(cast));
	}
}

class JumpingTestUI extends PlayView implements VisualController {

	public JumpingTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> false;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		cast().ghosts().forEach(cast()::putOnStage);
	}

	@Override
	public void update() {
		super.update();
		cast().ghostsOnStage().forEach(Ghost::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}