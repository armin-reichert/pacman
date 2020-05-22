package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.games.pacman.PacManApp;
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
		settings.title = "Pac-Man Follows Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestUI(new Game(), new ArcadeTheme()));
	}
}

class FollowMouseTestUI extends PlayView {

	private Tile mousePosition;

	public FollowMouseTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		theme.snd_ghost_chase().volume(0);
		game.stage.add(game.blinky);
		game.blinky.behavior(CHASING, game.blinky.isHeadingFor(() -> mousePosition));
		game.blinky.setState(CHASING);
		readMouse();
	}

	private void readMouse() {
		mousePosition = new Tile(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
	}

	@Override
	public void update() {
		super.update();
		if (Mouse.moved()) {
			readMouse();
		}
		game.blinky.update();
	}
}