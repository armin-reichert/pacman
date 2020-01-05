package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class MovingRandomlyTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new MovingRandomlyTestApp(), args);
	}

	public MovingRandomlyTestApp() {
		settings().title = "Moving Randomly";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new MovingRandomlyTestUI(cast));
	}
}

class MovingRandomlyTestUI extends PlayView implements VisualController {

	boolean started;

	public MovingRandomlyTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		cast().ghosts().forEach(ghost -> {
			cast().setActorOnStage(ghost);
			ghost.placeHalfRightOf(maze().pacManHome);
			ghost.setState(FRIGHTENED);
			ghost.during(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack());
		});
		message("Press SPACE");
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			started = true;
			clearMessage();
		}
		if (started) {
			cast().ghostsOnStage().forEach(Ghost::update);
		}
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}