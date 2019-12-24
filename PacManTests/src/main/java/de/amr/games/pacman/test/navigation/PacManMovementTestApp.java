package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.Steerings.avoidingGhosts;
import static de.amr.games.pacman.actor.behavior.Steerings.followsKeys;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		launch(new PacManMovementTestApp(), args);
	}

	public PacManMovementTestApp() {
		settings.title = "Pac-Man Movement";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new PacManMovementTestUI(cast));
	}
}

class PacManMovementTestUI extends PlayView implements VisualController {

	public PacManMovementTestUI(PacManGameCast cast) {
		super(cast);
		showScores(false);
		showGrid(true);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		cast().pacMan.addEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				theme().snd_eatPill().play();
				foodFound.tile.removeFood();
				game().level.numPelletsEaten++;
				if (game().numPelletsRemaining() == 0) {
					maze().restoreFood();
					game().level.numPelletsEaten = 0;
				}
			}
		});
		cast().setActorOnStage(cast().pacMan);
		message("Cursor keys");

	}

	@Override
	public void update() {
		super.update();
		handleSteeringChange();
		cast().actorsOnStage().forEach(MazeResident::update);
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_M)) {
			cast().pacMan
					.steering(followsKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
			message("Cursor keys");
		}
		else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_N)) {
			cast().pacMan.steering(
					followsKeys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
			message("Numpad keys");
		}
		else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_A)) {
			cast().pacMan.steering(avoidingGhosts());
			message("Avoiding ghosts");
		}
		else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_R)) {
			cast().pacMan.steering(isMovingRandomlyWithoutTurningBack());
			message("Random moves");
		}
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}