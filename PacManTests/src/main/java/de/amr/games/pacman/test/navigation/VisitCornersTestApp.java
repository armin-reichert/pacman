package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.Tile;

public class VisitCornersTestApp extends Application {

	public static void main(String[] args) {
		launch(VisitCornersTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Visit Corners";
	}

	@Override
	public void init() {
		setController(new FollowTargetTilesTestUI());
	}
}

class FollowTargetTilesTestUI extends TestUI {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI() {
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
		targets = Arrays.asList(world.cornerNW(), world.cornerNE(), world.cornerSE(), world.pacManSeat().tile,
				world.cornerSW());
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		world.eatFood();
		theme.snd_ghost_chase().volume(0);
		world.takePart(world.blinky);
		world.blinky.placeAt(targets.get(0));
		world.blinky.behavior(CHASING, world.blinky.isHeadingFor(() -> targets.get(current)));
		world.blinky.setState(CHASING);
		world.blinky.steering().force();
	}

	@Override
	public void update() {
		if (world.blinky.tile().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
				world.eatFood();
			}
		}
		world.blinky.update();
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