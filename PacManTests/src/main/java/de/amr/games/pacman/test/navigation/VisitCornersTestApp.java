package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.test.TestUI;

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

	@Override
	public void init() {
		super.init();
		targets = Arrays.asList(world.capeNW(), world.capeNE(), world.capeSE(), world.pacManBed().tile, world.capeSW());
		current = 0;
		soundManager.snd_ghost_chase().volume(0);
		include(blinky);
		blinky.init();
		blinky.placeAt(targets.get(0));
		blinky.behavior(GhostState.CHASING, blinky.headingFor(() -> targets.get(current)));
		blinky.setState(GhostState.CHASING);
		blinky.steering().force();
		view.turnRoutesOn();
		view.turnGridOn();
	}

	@Override
	public void update() {
		if (blinky.location().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
			}
		}
		super.update();
	}
}