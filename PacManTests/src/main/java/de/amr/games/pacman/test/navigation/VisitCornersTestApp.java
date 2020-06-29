package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
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

	private Ghost ghost;
	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI() {
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
		targets = Arrays.asList(world.cornerNW(), world.cornerNE(), world.cornerSE(), world.pacManBed().tile,
				world.cornerSW());
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		world.removeFood();
		theme.snd_ghost_chase().volume(0);
		ghost = world.blinky();
		world.putOnStage(ghost, true);
		ghost.placeAt(targets.get(0));
		ghost.behavior(GhostState.CHASING, ghost.headingFor(() -> targets.get(current)));
		ghost.setState(GhostState.CHASING);
		ghost.steering().force();
	}

	@Override
	public void update() {
		if (ghost.tile().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
				world.removeFood();
			}
		}
		ghost.update();
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