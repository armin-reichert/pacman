package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.behavior.Steerings.isHeadingFor;

import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class FollowMouseTestApp extends Application {

	public static void main(String[] args) {
		launch(new FollowMouseTestApp(), args);
	}

	public FollowMouseTestApp() {
		settings.title = "Follow Mouse";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
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
		game().init();
		maze().removeFood();
		theme().snd_ghost_chase().volume(0);
		cast().setActorOnStage(cast().blinky);
		cast().blinky.during(CHASING, isHeadingFor(() -> mouseTile));
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