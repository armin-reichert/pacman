package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.actor.core.MovingActor;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(PacManMovementTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Pac-Man Movement";
	}

	@Override
	public void init() {
		Game game = new Game();
		Cast cast = new Cast(game);
		Theme theme = new ArcadeTheme();
		setController(new PacManMovementTestUI(cast, theme));
	}
}

class PacManMovementTestUI extends PlayView implements VisualController {

	private PacMan pacMan;

	public PacManMovementTestUI(Cast cast, Theme theme) {
		super(cast, theme);
		pacMan = cast.pacMan;
		showRoutes = () -> false;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> true;
	}

	@Override
	public void init() {
		super.init();
		pacMan.addEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				theme.snd_eatPill().play();
				foodFound.tile.removeFood();
				game().level().numPelletsEaten++;
				if (game().numPelletsRemaining() == 0) {
					maze().restoreFood();
					game().level().numPelletsEaten = 0;
				}
			}
		});
		pacMan.setActing(true);
		pacMan.setState(PacManState.EATING);
		message("Cursor keys");
		startEnergizerBlinking();
	}

	@Override
	public void update() {
		super.update();
		handleSteeringChange();
		cast.movingActorsOnStage().forEach(MovingActor::update);
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_M)) {
			pacMan.behavior(pacMan.isFollowingKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
			message("Cursor keys");
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_N)) {
			pacMan.behavior(
					pacMan.isFollowingKeys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
			message("Numpad keys");
			// } else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_A)) {
			// pac.steering(avoidingGhosts(cast));
			// message("Avoiding ghosts");
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_R)) {
			pacMan.behavior(pacMan.isMovingRandomlyWithoutTurningBack());
			message("Random moves");
		}
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}