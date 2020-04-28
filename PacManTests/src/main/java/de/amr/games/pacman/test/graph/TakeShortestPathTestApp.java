package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class TakeShortestPathTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(TakeShortestPathTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Take Shortest Path";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new TakeShortestPathTestUI(game, theme));
	}
}

class TakeShortestPathTestUI extends PlayView implements VisualController {

	final Ghost ghost;
	final List<Tile> targets;
	int targetIndex;

	public TakeShortestPathTestUI(Game game, Theme theme) {
		super(game, theme);
		ghost = game.blinky;
		targets = Arrays.asList(game.maze.cornerSE, game.maze.tileAt(15, 23), game.maze.tileAt(12, 23), game.maze.cornerSW,
				game.maze.tileToDir(game.maze.portalLeft, Direction.RIGHT), game.maze.cornerNW, game.maze.ghostHouseSeats[0],
				game.maze.cornerNE, game.maze.tileToDir(game.maze.portalRight, Direction.LEFT), game.maze.pacManHome);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		targetIndex = 0;
		theme.snd_ghost_chase().volume(0);
		game.pushActorOnStage(ghost);
		Steering steering = ghost.isTakingShortestPath(() -> targets.get(targetIndex));
		ghost.behavior(CHASING, steering);
		ghost.behavior(FRIGHTENED, steering);
		ghost.setState(CHASING);
		message("SPACE toggles ghost state");
	}

	private void nextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game.enterLevel(game.level.number + 1);
			game.maze.removeFood();
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