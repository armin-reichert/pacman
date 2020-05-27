package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.actor.MovingActor;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		launch(PacManMovementTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Pac-Man Movement";
	}

	@Override
	public void init() {
		setController(new PacManMovementTestUI(new Game(), new ArcadeTheme()));
	}
}

class PacManMovementTestUI extends PlayView {

	private PacMan pacMan;

	public PacManMovementTestUI(Game game, Theme theme) {
		super(game, theme);
		pacMan = game.pacMan;
		showRoutes = false;
		showStates = false;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		pacMan.addEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				theme.snd_eatPill().play();
				game.maze.removeFood(foodFound.tile);
				game.level.eatenFoodCount++;
				if (game.remainingFoodCount() == 0) {
					game.maze.restoreFood();
					game.level.eatenFoodCount = 0;
				}
			}
		});
		game.stage.add(pacMan);
		pacMan.setState(PacManState.EATING);
		message.text = "Cursor keys";
		mazeView.energizersBlinking.setEnabled(true);
	}

	@Override
	public void update() {
		super.update();
		handleSteeringChange();
		game.movingActorsOnStage().forEach(MovingActor::update);
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_M)) {
			pacMan.behavior(pacMan.isFollowingKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
			message.text = "Cursor keys";
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_N)) {
			pacMan.behavior(
					pacMan.isFollowingKeys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
			message.text = "Numpad keys";
			// } else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_A)) {
			// pac.steering(avoidingGhosts(game));
			// message("Avoiding ghosts");
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_R)) {
			pacMan.behavior(pacMan.isMovingRandomlyWithoutTurningBack());
			message.text = "Random moves";
		}
	}
}