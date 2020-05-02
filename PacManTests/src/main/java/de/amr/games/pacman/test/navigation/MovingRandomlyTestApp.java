package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class MovingRandomlyTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(MovingRandomlyTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Moving Randomly";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new MovingRandomlyTestUI(game, theme));
	}
}

class MovingRandomlyTestUI extends PlayView implements VisualController {

	boolean started;

	public MovingRandomlyTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		game.ghosts().forEach(ghost -> {
			game.stage.add(ghost);
			ghost.tf.setPosition(game.maze.pacManHome.centerX(), game.maze.pacManHome.y());
			ghost.behavior(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack());
			ghost.state(FRIGHTENED).removeTimer();
			ghost.setState(FRIGHTENED);
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
			game.ghostsOnStage().forEach(Ghost::update);
		}
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}