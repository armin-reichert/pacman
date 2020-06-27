package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		PacManStateMachineLogging.setEnabled(true);
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
		setController(new PacManMovementTestUI());
	}
}

class PacManMovementTestUI extends PlayView {

	private PacMan pacMan;

	public PacManMovementTestUI() {
		super(new Game(), new ArcadeTheme());
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
				game.world.eatFood(foodFound.tile);
				game.level.eatenFoodCount++;
				if (game.remainingFoodCount() == 0) {
					game.world.restoreFood();
					game.level.eatenFoodCount = 0;
				}
			}
		});
		game.takePart(pacMan);
		pacMan.setState(PacManState.EATING);
		showMessage("Cursor keys", Color.WHITE);
		mazeView.energizersBlinking.setEnabled(true);
	}

	@Override
	public void update() {
		super.update();
		handleSteeringChange();
		game.creaturesOnStage().forEach(Creature::update);
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_M)) {
			pacMan.behavior(pacMan.isFollowingKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
			showMessage("Cursor keys", Color.WHITE);
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_N)) {
			pacMan.behavior(
					pacMan.isFollowingKeys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
			showMessage("Numpad keys", Color.WHITE);
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_R)) {
			pacMan.behavior(pacMan.isMovingRandomlyWithoutTurningBack());
			showMessage("Move randomly", Color.WHITE);
		}
	}
}