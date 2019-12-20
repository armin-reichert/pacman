package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steerings;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class MovingRandomlyTestApp extends Application {

	public static void main(String[] args) {
		launch(new MovingRandomlyTestApp(), args);
	}

	public MovingRandomlyTestApp() {
		settings.title = "Moving Randomly";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new MovingRandomlyTestUI(cast));
	}
}

class MovingRandomlyTestUI extends PlayView implements VisualController {

	boolean running;

	public MovingRandomlyTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(false);
		setShowScores(false);
		setShowGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().ghosts().forEach(ghost -> {
			cast().putOnStage(ghost);
			ghost.placeAtTile(maze().pacManHome, Tile.SIZE / 2, 0);
			ghost.setState(GhostState.CHASING);
			ghost.during(GhostState.CHASING, Steerings.isMovingRandomlyWithoutTurningBack());
		});
		messageText = "Press SPACE";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			running = true;
			messageText = null;
		}
		if (running) {
			cast().ghostsOnStage().forEach(Ghost::update);
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}