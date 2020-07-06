package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.Portal;
import de.amr.games.pacman.model.world.core.Tile;
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
		setController(new TakeShortestPathTestUI());
	}
}

class TakeShortestPathTestUI extends TestUI {

	private List<Tile> targets;
	private int targetIndex;

	@Override
	public void init() {
		super.init();
		view.turnRoutesOn();
		view.turnStatesOn();
		view.turnGridOn();
		Portal thePortal = world.portals().findAny().get();
		House theHouse = world.theHouse();
		targets = Arrays.asList(world.capeSE(), Tile.at(15, 23), Tile.at(12, 23), world.capeSW(),
				world.neighbor(thePortal.left, Direction.RIGHT), world.capeNW(), theHouse.bed(0).tile, world.capeNE(),
				world.neighbor(thePortal.right, Direction.LEFT), world.pacManBed().tile);
		targetIndex = 0;
		soundManager.snd_ghost_chase().volume(0);
		include(blinky);
		Steering steering = blinky.takingShortestPath(() -> targets.get(targetIndex));
		blinky.behavior(CHASING, steering);
		blinky.behavior(FRIGHTENED, steering);
		blinky.setState(CHASING);
		view.showMessage(2, "SPACE toggles ghost state", Color.WHITE);
	}

	private void nextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game.enterLevel(game.level.number + 1);
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			blinky.setState(blinky.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		if (blinky.tile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		super.update();
	}
}