package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class FollowTargetTilesTestApp extends Application {

	public static void main(String[] args) {
		launch(FollowTargetTilesTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Follow Target Tiles";
	}

	@Override
	public void init() {
		setController(new FollowTargetTilesTestUI());
	}
}

class FollowTargetTilesTestUI extends PlayView {

	private List<Tile> targets;
	private int current;
	private Maze maze;

	public FollowTargetTilesTestUI() {
		super(new Game(), new ArcadeTheme());
		maze = game.maze;
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;

		targets = Arrays.asList(maze.cornerNW, maze.ghostSeats[0].tile, maze.cornerNE, maze.cornerSE, maze.pacManSeat.tile,
				maze.cornerSW);
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		maze.eatAllFood();
		theme.snd_ghost_chase().volume(0);
		game.takePart(game.blinky);
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
				maze.eatAllFood();
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