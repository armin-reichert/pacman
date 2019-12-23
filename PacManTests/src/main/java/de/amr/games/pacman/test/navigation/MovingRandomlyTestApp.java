package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
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

	boolean started;

	public MovingRandomlyTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(true);
		showStates(true);
		setShowScores(false);
		showGrid(true);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().ghosts().forEach(ghost -> {
			cast().putOnStage(ghost);
			ghost.placeAtTile(maze().pacManHome, Tile.SIZE / 2, 0);
			ghost.setState(FRIGHTENED);
			ghost.during(FRIGHTENED, isMovingRandomlyWithoutTurningBack());
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
	public View currentView() {
		return this;
	}
}