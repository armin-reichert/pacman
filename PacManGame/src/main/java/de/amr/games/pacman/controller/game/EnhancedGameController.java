package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.PacManApp.fsm_logging;
import static de.amr.games.pacman.PacManApp.fsm_logging_enabled;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.game.PacManGameState.PLAYING;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;

/**
 * Enhanced game controller with all the bells and whistles.
 * 
 * @author Armin Reichert
 */
public class EnhancedGameController extends GameController {

	@Override
	public void update() {
		handleInput();
		super.update();
	}

	private void handleInput() {
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_LEFT)) {
			int oldFreq = app().clock().getTargetFramerate();
			changeClockFrequency(oldFreq <= 10 ? Math.max(1, oldFreq - 1) : oldFreq - 5);
		}

		else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_RIGHT)) {
			int oldFreq = app().clock().getTargetFramerate();
			changeClockFrequency(oldFreq < 10 ? oldFreq + 1 : oldFreq + 5);
		}

		if (currentView == playView) {
			handlePlayViewInput();
		}
	}

	private void handlePlayViewInput() {
		if (Keyboard.keyPressedOnce("b")) {
			toggleGhostOnStage(folks.blinky());
		}

		else if (Keyboard.keyPressedOnce("c")) {
			toggleGhostOnStage(folks.clyde());
		}

		else if (Keyboard.keyPressedOnce("d")) {
			toggleDemoMode();
		}

		else if (Keyboard.keyPressedOnce("e")) {
			eatAllSimplePellets();
		}

		else if (Keyboard.keyPressedOnce("f")) {
			toggleGhostFrightenedBehavior();
		}

		else if (Keyboard.keyPressedOnce("g")) {
			setShowingGrid(!isShowingGrid());
		}

		else if (Keyboard.keyPressedOnce("i")) {
			toggleGhostOnStage(folks.inky());
		}

		else if (Keyboard.keyPressedOnce("k")) {
			killAllGhosts();
		}

		else if (Keyboard.keyPressedOnce("l")) {
			toggleStateMachineLogging();
		}

		else if (Keyboard.keyPressedOnce("m")) {
			toggleMakePacManImmortable();
		}

		else if (Keyboard.keyPressedOnce("o")) {
			togglePacManOverflowBug();
		}

		else if (Keyboard.keyPressedOnce("p")) {
			toggleGhostOnStage(folks.pinky());
		}

		else if (Keyboard.keyPressedOnce("s")) {
			setShowingStates(!isShowingStates());
		}

		else if (Keyboard.keyPressedOnce("t")) {
			if (playView.isShowingFrameRate()) {
				playView.turnFrameRateOff();
			} else {
				playView.turnFrameRateOn();
			}
		}

		else if (Keyboard.keyPressedOnce("r")) {
			setShowingRoutes(!isShowingRoutes());
		}

		else if (Keyboard.keyPressedOnce("x")) {
			toggleGhostsHarmless();
		}

		else if (Keyboard.keyPressedOnce("+")) {
			switchToNextLevel();
		}

	}

	private void toggleGhostOnStage(Ghost ghost) {
		if (world.contains(ghost)) {
			world.takeOut(ghost);
		} else {
			world.bringIn(ghost);
			ghost.init();
		}
	}

	private void togglePacManOverflowBug() {
		settings.fixOverflowBug = !settings.fixOverflowBug;
		loginfo("Overflow bug is %s", settings.fixOverflowBug ? "fixed" : "active");
	}

	private void toggleStateMachineLogging() {
		fsm_logging(!fsm_logging_enabled());
		loginfo("State machine logging %s", fsm_logging_enabled() ? "enabled" : "disabled");
	}

	private void toggleGhostFrightenedBehavior() {
		if (settings.ghostsSafeCorner) {
			settings.ghostsSafeCorner = false;
			folks.ghosts().forEach(ghost -> ghost.behavior(FRIGHTENED, ghost.movingRandomly()));
			loginfo("Ghost escape behavior is: Random movement");
		} else {
			settings.ghostsSafeCorner = true;
			folks.ghosts().forEach(ghost -> ghost.behavior(FRIGHTENED, ghost.fleeingToSafeCorner(folks.pacMan())));
			loginfo("Ghosts escape behavior is: Fleeing to safe corners");
		}
	}

	private void toggleGhostsHarmless() {
		settings.ghostsHarmless = !settings.ghostsHarmless;
		loginfo("Ghosts are %s", settings.ghostsHarmless ? "harmless" : "dangerous");
	}

	private void toggleDemoMode() {
		settings.demoMode = !settings.demoMode;
		setDemoMode(settings.demoMode);
		loginfo("Demo mode is %s", settings.demoMode ? "on" : "off");
	}

	private void toggleMakePacManImmortable() {
		settings.pacManImmortable = !settings.pacManImmortable;
		loginfo("Pac-Man immortable = %s", settings.pacManImmortable);
	}

	private void switchToNextLevel() {
		loginfo("Switching to level %d", game.level.number + 1);
		enqueue(new LevelCompletedEvent());
	}

	private void eatAllSimplePellets() {
		if (getState() != PLAYING) {
			return;
		}
		world.habitatTiles().filter(world::containsSimplePellet).forEach(tile -> {
			world.clearFood(tile);
			game.scoreSimplePelletFound();
			doorMan().ifPresent(houseAccess -> {
				houseAccess.onPacManFoundFood();
				houseAccess.update();
			});
		});
		loginfo("All simple pellets have been eaten");
		if (game.level.remainingFoodCount() == 0) {
			enqueue(new LevelCompletedEvent());
			return;
		}
	}

	private void killAllGhosts() {
		if (getState() != PLAYING) {
			return;
		}
		game.level.ghostsKilledByEnergizer = 0;
		folks.ghostsInsideWorld().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
			game.scoreGhostKilled(ghost.name());
			ghost.process(new GhostKilledEvent(ghost));
		});
		loginfo("All ghosts have been killed");
	}
}