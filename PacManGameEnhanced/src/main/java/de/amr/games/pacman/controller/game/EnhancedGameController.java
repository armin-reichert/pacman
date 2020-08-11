package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.game.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.game.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.game.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.game.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacman.controller.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.play.EnhancedPlayView;
import de.amr.games.pacman.view.play.PlayView;

/**
 * Enhanced game controller with all the bells and whistles.
 * 
 * @author Armin Reichert
 */
public class EnhancedGameController extends GameController {

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;
	private boolean showingScores = true;

	/**
	 * Creates a new game controller.
	 * 
	 * @param themes supported themes
	 */
	public EnhancedGameController(Theme... themes) {
		super(themes);
		addStateEntryListener(INTRO, state -> {
			REGISTRY.register(currentView.machines());
		});
		addStateExitListener(INTRO, state -> {
			REGISTRY.unregister(currentView.machines());
		});
		addStateEntryListener(GETTING_READY, state -> {
			REGISTRY.register(currentView.machines());
			folks.all().forEach(guy -> REGISTRY.register(guy.machines()));
			REGISTRY.register(Stream.of(this, bonusControl, ghostCommand));
		});
		addStateEntryListener(GAME_OVER, state -> {
			REGISTRY.unregister(currentView.machines());
			folks.all().forEach(guy -> REGISTRY.unregister(guy.machines()));
			REGISTRY.unregister(Stream.of(this, bonusControl, ghostCommand));
		});
	}

	@Override
	protected PlayView createPlayView() {
		return new EnhancedPlayView(world, getTheme(), folks, game, ghostCommand);
	}

	protected EnhancedPlayView playView() {
		return (EnhancedPlayView) playView;
	}

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

		else if (Keyboard.keyPressedOnce("l")) {
			if (!app().getLogger().isShutUp()) {
				loginfo("Application Logging is OFF");
			}
			app().getLogger().shutUp(!app().getLogger().isShutUp());
			if (!app().getLogger().isShutUp()) {
				loginfo("Application Logging is ON");
			}
		}

		if (currentView == playView) {
			handlePlayViewInput();
		}
	}

	private void handlePlayViewInput() {
		if (Keyboard.keyPressedOnce("b")) {
			toggleGhostInWorld(folks.blinky);
		}

		else if (Keyboard.keyPressedOnce("c")) {
			toggleGhostInWorld(folks.clyde);
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
			toggleGhostInWorld(folks.inky);
		}

		else if (Keyboard.keyPressedOnce("k")) {
			killAllGhosts();
		}

		else if (Keyboard.keyPressedOnce("m")) {
			toggleMakePacManImmortable();
		}

		else if (Keyboard.keyPressedOnce("o")) {
			togglePacManOverflowBug();
		}

		else if (Keyboard.keyPressedOnce("p")) {
			toggleGhostInWorld(folks.pinky);
		}

		else if (Keyboard.keyPressedOnce("s")) {
			setShowingStates(!isShowingStates());
		}

		else if (Keyboard.keyPressedOnce("t")) {
			if (playView().isShowingFrameRate()) {
				playView().turnFrameRateOff();
			} else {
				playView().turnFrameRateOn();
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

	protected void setDemoMode(boolean demoMode) {
		if (demoMode) {
			settings.pacManImmortable = true;
			folks.pacMan.setWalkingBehavior(new SearchingForFoodAndAvoidingGhosts(folks));
			playView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
		} else {
			settings.pacManImmortable = false;
			you(folks.pacMan).followTheKeys().keys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT).ok();
			playView.clearMessage(1);
		}
	}

	public void setShowingRoutes(boolean selected) {
		showingRoutes = selected;
		if (selected) {
			playView().turnRoutesOn();
		} else {
			playView().turnRoutesOff();
		}
	}

	public boolean isShowingRoutes() {
		return showingRoutes;
	}

	public void setShowingGrid(boolean selected) {
		showingGrid = selected;
		if (selected) {
			playView().turnGridOn();
		} else {
			playView().turnGridOff();
		}
	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public void setShowingStates(boolean selected) {
		showingStates = selected;
		if (selected) {
			playView().turnStatesOn();
		} else {
			playView().turnStatesOff();
		}
	}

	public boolean isShowingStates() {
		return showingStates;
	}

	public void setShowingScores(boolean selected) {
		showingScores = selected;
		if (selected) {
			playView.turnScoresOn();
		} else {
			playView.turnScoresOff();
		}
	}

	public boolean isShowingScores() {
		return showingScores;
	}

	private void toggleGhostInWorld(Ghost ghost) {
		if (world.contains(ghost)) {
			world.exclude(ghost);
		} else {
			world.include(ghost);
			ghost.init();
		}
	}

	private void togglePacManOverflowBug() {
		settings.fixOverflowBug = !settings.fixOverflowBug;
		loginfo("Overflow bug is %s", settings.fixOverflowBug ? "fixed" : "active");
	}

	private void toggleGhostFrightenedBehavior() {
		if (settings.ghostsSafeCorner) {
			settings.ghostsSafeCorner = false;
			folks.ghosts().forEach(ghost -> you(ghost).when(FRIGHTENED).moveRandomly().ok());
			loginfo("Ghost escape behavior is: Random movement");
		} else {
			settings.ghostsSafeCorner = true;
			folks.ghosts().forEach(ghost -> ghost.behavior(FRIGHTENED, new FleeingToSafeTile(ghost, folks.pacMan)));
			loginfo("Ghosts escape behavior is: Fleeing to safe corners");
		}
	}

	private void toggleGhostsHarmless() {
		settings.ghostsHarmless = !settings.ghostsHarmless;
		loginfo("Ghosts are %s", settings.ghostsHarmless ? "harmless" : "dangerous");
	}

	public void toggleDemoMode() {
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
		world.tiles().filter(location -> world.hasFood(Pellet.SNACK, location)).forEach(tile -> {
			world.clearFood(tile);
			game.scoreSimplePelletFound();
			doorMan().ifPresent(doorMan -> {
				doorMan.onPacManFoundFood();
				doorMan.update();
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
		folks.ghostsInWorld().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
			game.scoreGhostKilled(ghost.name);
			ghost.process(new GhostKilledEvent(ghost));
		});
		loginfo("All ghosts have been killed");
	}
}