package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Optional;

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
		launch(new OutsideTileTestApp(), args);
	}

	public OutsideTileTestApp() {
		settings().title = "Follow Tile Outside Maze";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new OutsideTileTestUI(cast));
	}
}

class OutsideTileTestUI extends PlayView implements VisualController {

	public OutsideTileTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> true;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		theme().snd_ghost_chase().volume(0);
		cast().putActorOnStage(cast().blinky);
		cast().blinky.behavior(CHASING, cast().blinky.isHeadingFor(() -> maze().tileAt(100, maze().portalRight.row)));
		cast().blinky.setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		cast().blinky.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}