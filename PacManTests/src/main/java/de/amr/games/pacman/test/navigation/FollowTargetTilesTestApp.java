package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class FollowTargetTilesTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(FollowTargetTilesTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Follow Target Tiles";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new FollowTargetTilesTestUI(game, theme));
	}
}

class FollowTargetTilesTestUI extends PlayView implements VisualController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
		targets = Arrays.asList(game.maze.cornerNW, game.maze.ghostHouseEntry, game.maze.cornerNE, game.maze.cornerSE,
				game.maze.pacManHome, game.maze.cornerSW);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		game.maze.removeFood();
		theme.snd_ghost_chase().volume(0);
		game.stage.add(game.blinky);
		game.blinky.placeAt(targets.get(0));
		game.blinky.behavior(CHASING, game.blinky.isHeadingFor(() -> targets.get(current)));
		game.blinky.setState(CHASING);
		game.blinky.steering().force();
	}

	@Override
	public void update() {
		if (game.blinky.tile().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
				game.maze.removeFood();
			}
		}
		game.blinky.update();
		super.update();
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		try (Pen pen = new Pen(g)) {
			pen.color(Color.YELLOW);
			pen.fontSize(8);
			for (int i = 0; i < targets.size(); ++i) {
				Tile target = targets.get(i);
				pen.drawAtGridPosition(String.valueOf(i), target.col, target.row, Tile.SIZE);
			}
		}
	}
}