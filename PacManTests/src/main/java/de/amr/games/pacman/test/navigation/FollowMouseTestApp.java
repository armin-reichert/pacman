package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class FollowMouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(FollowMouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Follow Mouse";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new FollowMouseTestUI(cast));
	}
}

class FollowMouseTestUI extends PlayView implements VisualController {

	private Tile mouseTile;

	public FollowMouseTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> true;
		showStates = () -> false;
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
		theme().snd_ghost_chase().volume(0);
		cast().putActorOnStage(cast().blinky);
		cast().blinky.behavior(CHASING, cast().blinky.isHeadingFor(() -> mouseTile));
		cast().blinky.setState(CHASING);
		readMouse();
	}

	private void readMouse() {
		mouseTile = maze().tileAt(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
	}

	@Override
	public void update() {
		super.update();
		if (Mouse.moved()) {
			readMouse();
		}
		cast().blinky.update();
	}
}