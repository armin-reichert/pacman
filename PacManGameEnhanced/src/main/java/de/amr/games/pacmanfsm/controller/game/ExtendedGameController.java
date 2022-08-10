/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.game;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.GAME_OVER;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.GETTING_READY;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.INTRO;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.PLAYING;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.pacman.PacManState;
import de.amr.games.pacmanfsm.controller.event.GhostKilledEvent;
import de.amr.games.pacmanfsm.controller.event.LevelCompletedEvent;
import de.amr.games.pacmanfsm.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacmanfsm.controller.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeFood;
import de.amr.games.pacmanfsm.model.world.graph.WorldGraph;
import de.amr.games.pacmanfsm.theme.api.Theme;
import de.amr.games.pacmanfsm.view.play.ExtendedPlayView;
import de.amr.games.pacmanfsm.view.play.PlayView;

/**
 * Game controller with additional functionality that is not needed for just playing the game.
 * 
 * @author Armin Reichert
 */
public class ExtendedGameController extends GameController {

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;
	private boolean showingScores = true;
	private WorldGraph graph;

	public ExtendedGameController(List<Theme> themes) {
		super(themes);
		Stream.of(this, bonusController, ghostCommand).forEach(fsm -> REGISTRY.register("Game", fsm));
		addStateEntryListener(INTRO,
				state -> currentView.machines().forEach(fsm -> REGISTRY.register(currentView.getClass().getSimpleName(), fsm)));
		addStateExitListener(INTRO, state -> currentView.machines().forEach(REGISTRY::unregister));
		addStateEntryListener(GETTING_READY, state -> {
			currentView.machines().forEach(fsm -> REGISTRY.register(currentView.getClass().getSimpleName(), fsm));
			folks.pacMan.machines().forEach(REGISTRY::unregister);
			folks.pacMan.machines().forEach(fsm -> REGISTRY.register("Pac-Man", fsm));
			folks.ghosts().forEach(ghost -> {
				ghost.machines().forEach(REGISTRY::unregister);
				ghost.machines().forEach(fsm -> REGISTRY.register("Ghosts", fsm));
			});
		});
		addStateEntryListener(GAME_OVER, state -> currentView.machines().forEach(REGISTRY::unregister));
	}

	@Override
	public void init() {
		super.init();
		setDemoMode(appSettings().demoMode);
	}

	@Override
	protected PlayView createPlayView() {
		return new ExtendedPlayView(appSettings(), themes.current(), folks, ghostCommand, world);
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
			appSettings().pacManImmortable = true;
			folks.pacMan.setSteering(PacManState.AWAKE,
					new SearchingForFoodAndAvoidingGhosts(appSettings(), world, folks.pacMan, folks));
		} else {
			appSettings().pacManImmortable = false;
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
		appSettings().fixOverflowBug = !appSettings().fixOverflowBug;
		loginfo("Overflow bug is %s", appSettings().fixOverflowBug ? "fixed" : "active");
	}

	private void toggleGhostFrightenedBehavior() {
		if (appSettings().ghostsSafeCorner) {
			appSettings().ghostsSafeCorner = false;
			folks.ghosts().forEach(ghost -> you(ghost).when(FRIGHTENED).moveRandomly().ok());
			loginfo("Ghost escape behavior is: Random movement");
		} else {
			appSettings().ghostsSafeCorner = true;
			if (graph == null) {
				graph = new WorldGraph(appSettings(), world);
			}
			folks.ghosts().forEach(ghost -> ghost.setSteering(FRIGHTENED, new FleeingToSafeTile(ghost, graph, folks.pacMan)));
			loginfo("Ghosts escape behavior is: Fleeing to safe corners");
		}
	}

	private void toggleGhostsHarmless() {
		appSettings().ghostsHarmless = !appSettings().ghostsHarmless;
		loginfo("Ghosts are %s", appSettings().ghostsHarmless ? "harmless" : "dangerous");
	}

	public void toggleDemoMode() {
		appSettings().demoMode = !appSettings().demoMode;
		setDemoMode(appSettings().demoMode);
		if (appSettings().demoMode) {
			playView().messagesView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
		} else {
			playView().messagesView.clearMessage(1);
		}
		loginfo("Demo mode is %s", appSettings().demoMode ? "on" : "off");
	}

	private void toggleMakePacManImmortable() {
		appSettings().pacManImmortable = !appSettings().pacManImmortable;
		loginfo("Pac-Man immortable = %s", appSettings().pacManImmortable);
	}

	private void switchToNextLevel() {
		loginfo("Switching to level %d", theGame.level + 1);
		enqueue(new LevelCompletedEvent());
	}

	private void eatAllSimplePellets() {
		if (getState() != PLAYING) {
			return;
		}
		world.tiles().filter(location -> world.hasFood(ArcadeFood.PELLET, location)).forEach(tile -> {
			world.removeFood(tile);
			theGame.gainPelletPoints();
			doorMan.onPacManFoundFood();
			doorMan.update();
		});
		loginfo("All simple pellets have been eaten");
		if (theGame.remainingFoodCount() == 0) {
			enqueue(new LevelCompletedEvent());
		}
	}

	private void killAllGhosts() {
		if (getState() != PLAYING) {
			return;
		}
		theGame.ghostsKilledByEnergizer = 0;
		folks.ghostsInWorld().filter(ghost -> ghost.ai.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
			theGame.gainGhostPoints();
			ghost.ai.process(new GhostKilledEvent(ghost));
		});
		loginfo("All ghosts have been killed");
	}
}