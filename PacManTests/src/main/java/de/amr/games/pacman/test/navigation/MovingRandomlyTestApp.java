package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
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
		setController(new MovingRandomlyTestUI(new Game(), new ArcadeTheme()));
	}
}

class MovingRandomlyTestUI extends PlayView {

	boolean started;

	public MovingRandomlyTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
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
		message.text = "Press SPACE";
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			started = true;
			message.text = "";
		}
		if (started) {
			game.ghostsOnStage().forEach(Ghost::update);
		}
	}
}