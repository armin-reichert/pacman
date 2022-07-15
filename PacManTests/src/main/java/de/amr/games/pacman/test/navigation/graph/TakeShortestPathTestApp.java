package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.steering.common.FollowingPath;
import de.amr.games.pacman.controller.steering.common.TakingShortestPath;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.graph.WorldGraph;
import de.amr.games.pacman.test.TestController;

public class TakeShortestPathTestApp extends Application {

	public static void main(String[] args) {
		launch(TakeShortestPathTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Take Shortest Path";
	}

	@Override
	public void init() {
		soundManager().muteAll();
		setController(new TakeShortestPathTestUI());
	}
}

class TakeShortestPathTestUI extends TestController {

	private List<Tile> targets;
	private int targetIndex;

	@Override
	public void init() {
		super.init();

		List<Tile> capes = world.capes();
		Portal thePortal = world.portals().findAny().orElseThrow();
		//@formatter:off
		targets = Arrays.asList(
				capes.get(2), 
				Tile.at(15, 23), 
				Tile.at(12, 23), 
				capes.get(3),
				world.neighbor(thePortal.either, RIGHT), 
				capes.get(0),
				Tile.at(blinky.bed.col(), blinky.bed.row()), 
				capes.get(1),
				world.neighbor(thePortal.other, LEFT), 
				Tile.at(world.pacManBed().col(), 
				world.pacManBed().row()));
		//@formatter:on
		targetIndex = 0;

		WorldGraph graph = new WorldGraph(world);
		FollowingPath visitNextTarget = new TakingShortestPath(blinky, graph, () -> targets.get(targetIndex));
		blinky.setSteering(CHASING, visitNextTarget);
		blinky.setSteering(FRIGHTENED, visitNextTarget);
		blinky.ai.setState(CHASING);
		include(blinky);

		view.turnRoutesOn();
		view.turnStatesOn();
		view.turnGridOn();
		view.messagesView.showMessage(2, "SPACE toggles ghost state", Color.WHITE);
	}

	private void selectNextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
		}
	}

	private Tile currentTarget() {
		return targets.get(targetIndex);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			blinky.ai.setState(blinky.ai.getState() == CHASING ? FRIGHTENED : CHASING);
		}
		if (blinky.tile().equals(currentTarget())) {
			selectNextTarget();
		}
		super.update();
	}
}