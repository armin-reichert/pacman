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
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.you;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacman.controller.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.play.ExtendedPlayView;
import de.amr.games.pacman.view.play.PlayView;

/**
 * Game controller with additional functionaliy that is not needed for just playing the game.
 * 
 * @author Armin Reichert
 */
public class ExtendedGameController extends GameController {

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;
	private boolean showingScores = true;

	public ExtendedGameController(Stream<Theme> themes) {
		super(themes);
		REGISTRY.register("Game", this);
		REGISTRY.register("Game", Stream.of(bonusController, ghostCommand));
		addStateEntryListener(INTRO, state -> {
			REGISTRY.register(currentView.getClass().getSimpleName(), currentView.machines());
		});
		addStateExitListener(INTRO, state -> {
			REGISTRY.unregister(currentView.machines());
		});
		addStateEntryListener(GETTING_READY, state -> {
			REGISTRY.register(currentView.getClass().getSimpleName(), currentView.machines());
			REGISTRY.register(folks.pacMan.name, folks.pacMan.machines());
			folks.ghosts().forEach(ghost -> REGISTRY.register("Ghosts", ghost.machines()));
		});
		addStateEntryListener(GAME_OVER, state -> {
			REGISTRY.unregister(currentView.machines());
		});
	}

	@Override
	public void init() {
		super.init();
		setDemoMode(settings.demoMode);
	}

	@Override
	protected PlayView createPlayView() {
		return new ExtendedPlayView(themes.current(), folks, ghostCommand, world);
	}

	@Override
	protected void handleInput() {
		super.handleInput();
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_LEFT)) {
			int oldFreq = app().clock().getTargetFramerate();
			Timing.changeClockFrequency(oldFreq <= 10 ? Math.max(1, oldFreq - 1) : oldFreq - 5);
		}

		else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_RIGHT)) {
			int oldFreq = app().clock().getTargetFramerate();
			Timing.changeClockFrequency(oldFreq < 10 ? oldFreq + 1 : oldFreq + 5);
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

		if (currentView instanceof PlayView) {
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
			if (this.<ExtendedPlayView>playView().isShowingFrameRate()) {
				this.<ExtendedPlayView>playView().turnFrameRateOff();
			} else {
				this.<ExtendedPlayView>playView().turnFrameRateOn();
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
			folks.pacMan.setSteering(PacManState.AWAKE, new SearchingForFoodAndAvoidingGhosts(folks));
		} else {
			settings.pacManImmortable = false;
			you(folks.pacMan).followTheCursorKeys().ok();
		}
	}

	public void setShowingRoutes(boolean selected) {
		showingRoutes = selected;
		if (selected) {
			this.<ExtendedPlayView>playView().turnRoutesOn();
		} else {
			this.<ExtendedPlayView>playView().turnRoutesOff();
		}
	}

	public boolean isShowingRoutes() {
		return showingRoutes;
	}

	public void setShowingGrid(boolean selected) {
		showingGrid = selected;
		if (selected) {
			this.<ExtendedPlayView>playView().turnGridOn();
		} else {
			this.<ExtendedPlayView>playView().turnGridOff();
		}
	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public void setShowingStates(boolean selected) {
		showingStates = selected;
		if (selected) {
			this.<ExtendedPlayView>playView().turnStatesOn();
		} else {
			this.<ExtendedPlayView>playView().turnStatesOff();
		}
	}

	public boolean isShowingStates() {
		return showingStates;
	}

	public void setShowingScores(boolean selected) {
		showingScores = selected;
		if (selected) {
			this.<ExtendedPlayView>playView().turnScoresOn();
		} else {
			this.<ExtendedPlayView>playView().turnScoresOff();
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
			folks.ghosts().forEach(ghost -> ghost.setSteering(FRIGHTENED, new FleeingToSafeTile(ghost, ghost.pacMan)));
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
		if (settings.demoMode) {
			playView().messages.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
		} else {
			playView().messages.clearMessage(1);
		}
		loginfo("Demo mode is %s", settings.demoMode ? "on" : "off");
	}

	private void toggleMakePacManImmortable() {
		settings.pacManImmortable = !settings.pacManImmortable;
		loginfo("Pac-Man immortable = %s", settings.pacManImmortable);
	}

	private void switchToNextLevel() {
		loginfo("Switching to level %d", PacManGame.game.level + 1);
		enqueue(new LevelCompletedEvent());
	}

	private void eatAllSimplePellets() {
		if (getState() != PLAYING) {
			return;
		}
		world.tiles().filter(location -> world.hasFood(ArcadeFood.PELLET, location)).forEach(tile -> {
			world.removeFood(tile);
			PacManGame.game.scoreSimplePelletEaten();
			doorMan.onPacManFoundFood();
			doorMan.update();
		});
		loginfo("All simple pellets have been eaten");
		if (PacManGame.game.remainingFoodCount() == 0) {
			enqueue(new LevelCompletedEvent());
			return;
		}
	}

	private void killAllGhosts() {
		if (getState() != PLAYING) {
			return;
		}
		PacManGame.game.ghostsKilledByEnergizer = 0;
		folks.ghostsInWorld().filter(ghost -> ghost.ai.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
			PacManGame.game.scoreGhostKilled();
			ghost.ai.process(new GhostKilledEvent(ghost));
		});
		loginfo("All ghosts have been killed");
	}
}