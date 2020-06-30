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
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.test.navigation.TestUI;

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

	final Ghost ghost;
	final List<Tile> targets;
	int targetIndex;

	public TakeShortestPathTestUI() {
		ghost = world.blinky();
		Portal thePortal = world.portals().findAny().get();
		House theHouse = world.houses().findAny().get();
		targets = Arrays.asList(world.cornerSE(), Tile.at(15, 23), Tile.at(12, 23), world.cornerSW(),
				world.neighbor(thePortal.left, Direction.RIGHT), world.cornerNW(), theHouse.bed(0).tile, world.cornerNE(),
				world.neighbor(thePortal.right, Direction.LEFT), world.pacManBed().tile);
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.removeFood();
		targetIndex = 0;
		theme.snd_ghost_chase().volume(0);
		world.putOnStage(ghost, true);
		Steering steering = ghost.takingShortestPath(() -> targets.get(targetIndex));
		ghost.behavior(CHASING, steering);
		ghost.behavior(FRIGHTENED, steering);
		ghost.setState(CHASING);
		showMessage("SPACE toggles ghost state", Color.WHITE);
	}

	private void nextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game.enterLevel(game.level.number + 1);
		}
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			ghost.setState(ghost.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		ghost.update();
		if (ghost.tile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
	}
}