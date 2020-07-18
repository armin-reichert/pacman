package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.model.world.api.Tile;
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
		targets = Arrays.asList(world.capeNW(), world.capeNE(), world.capeSE(),
				Tile.at(world.pacManBed().col(), world.pacManBed().row()), world.capeSW());
		current = 0;
		soundManager.snd_ghost_chase().volume(0);
		include(blinky);
		blinky.init();
		blinky.placeAt(targets.get(0), 0, 0);
		you(blinky).when(CHASING).headFor().tile(() -> targets.get(current)).ok();
		blinky.setState(CHASING);
		blinky.steering().force();
		view.turnRoutesOn();
		view.turnGridOn();
	}

	@Override
	public void update() {
		if (blinky.tileLocation().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game.enterLevel(game.level.number + 1);
			}
		}
		super.update();
	}
}