package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.steering.common.FollowingPath;
import de.amr.games.pacman.controller.steering.common.TakingShortestPath;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class TakeShortestPathTestApp extends Application {

	public static void main(String[] args) {
		launch(TakeShortestPathTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Take Shortest Path";
	}

	@Override
	public void init() {
		soundManager().muteAll();
		setController(new TakeShortestPathTestUI());
	}
}

class TakeShortestPathTestUI extends TestUI {

	private List<Tile> targets;
	private int targetIndex;

	@Override
	public void init() {
		super.init();

		List<Tile> capes = world.capes();
		Portal thePortal = world.portals().findAny().get();
		//@formatter:off
		targets = Arrays.asList(
				capes.get(2), 
				Tile.at(15, 23), 
				Tile.at(12, 23), 
				capes.get(3),
				world.neighbor(thePortal.either, RIGHT), 
				capes.get(0),
				Tile.at(blinky.bed().col(), blinky.bed().row()), 
				capes.get(1),
				world.neighbor(thePortal.other, LEFT), 
				Tile.at(world.pacManBed().col(), 
				world.pacManBed().row()));
		//@formatter:on
		targetIndex = 0;

		FollowingPath visitNextTarget = new TakingShortestPath(blinky, () -> targets.get(targetIndex));
		blinky.behavior(CHASING, visitNextTarget);
		blinky.behavior(FRIGHTENED, visitNextTarget);
		blinky.setState(CHASING);
		include(blinky);

		view.turnRoutesOn();
		view.turnStatesOn();
		view.turnGridOn();
		view.showMessage(2, "SPACE toggles ghost state", Color.WHITE);
	}

	private void selectNextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game.enterLevel(game.level.number + 1);
		}
	}

	private Tile currentTarget() {
		return targets.get(targetIndex);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			blinky.setState(blinky.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		if (blinky.tileLocation().equals(currentTarget())) {
			selectNextTarget();
		}
		super.update();
	}
}