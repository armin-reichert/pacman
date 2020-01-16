package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class TakeShortestPathTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new TakeShortestPathTestApp(), args);
	}

	public TakeShortestPathTestApp() {
		settings().title = "Take Shortest Path";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new TakeShortestPathTestUI(cast));
	}
}

class TakeShortestPathTestUI extends PlayView implements VisualController {

	final Ghost ghost;
	final List<Tile> targets;
	int targetIndex;

	public TakeShortestPathTestUI(Cast cast) {
		super(cast);
		ghost = cast.blinky;
		targets = Arrays.asList(maze().cornerSE, maze().tileAt(15, 23), maze().tileAt(12, 23), maze().cornerSW,
				maze().tileToDir(maze().portalLeft, Direction.RIGHT), maze().cornerNW, maze().ghostHouseSeats[0],
				maze().cornerNE, maze().tileToDir(maze().portalRight, Direction.LEFT), maze().pacManHome);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		targetIndex = 0;
		cast.theme().snd_ghost_chase().volume(0);
		cast().putActorOnStage(ghost);
		Steering steering = ghost.isTakingShortestPath(() -> targets.get(targetIndex));
		ghost.during(CHASING, steering);
		ghost.during(FRIGHTENED, steering);
		ghost.setState(CHASING);
		message("SPACE toggles ghost state");
	}

	private void nextTarget() {
		if (++targetIndex == targets.size()) {
			targetIndex = 0;
			game().enterLevel(game().level().number + 1);
			maze().removeFood();
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