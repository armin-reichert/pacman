package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.PacManWorld;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

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

class TakeShortestPathTestUI extends PlayView implements VisualController {

	final Ghost ghost;
	final List<Tile> targets;
	int targetIndex;
	PacManWorld world;

	public TakeShortestPathTestUI() {
		super(new Game(), new ArcadeTheme());
		world = game.world;
		ghost = game.blinky;
		targets = Arrays.asList(world.cornerSE, Tile.xy(15, 23), Tile.xy(12, 23), world.cornerSW,
				world.neighbor(world.portal.left, Direction.RIGHT), world.cornerNW, world.ghostSeats.get(0).tile,
				world.cornerNE, world.neighbor(world.portal.right, Direction.LEFT), world.pacManSeat.tile);
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.eatAllFood();
		targetIndex = 0;
		theme.snd_ghost_chase().volume(0);
		game.takePart(ghost);
		Steering steering = ghost.isTakingShortestPath(() -> targets.get(targetIndex));
		ghost.behavior(CHASING, steering);
		ghost.behavior(FRIGHTENED, steering);
		ghost.setState(CHASING);
		showMessage("SPACE toggles ghost state", Color.WHITE);
	}

	private void nextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game.enterLevel(game.level.number + 1);
			world.eatAllFood();
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

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}