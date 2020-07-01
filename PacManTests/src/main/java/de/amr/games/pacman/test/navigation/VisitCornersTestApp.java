package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.world.core.Tile;

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
		view.showRoutes = true;
		view.showGrid = true;
		targets = Arrays.asList(world.cornerNW(), world.cornerNE(), world.cornerSE(), world.pacManBed().tile,
				world.cornerSW());
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		theme.snd_ghost_chase().volume(0);
		putOnStage(blinky);
		blinky.placeAt(targets.get(0));
		blinky.behavior(GhostState.CHASING, blinky.headingFor(() -> targets.get(current)));
		blinky.setState(GhostState.CHASING);
		blinky.steering().force();
	}

	@Override
	public void update() {
		if (blinky.tile().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
			}
		}
		super.update();
	}
}