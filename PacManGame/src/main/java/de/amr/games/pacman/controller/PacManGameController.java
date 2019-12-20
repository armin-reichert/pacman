package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.behavior.Steerings.isFleeingToSafeCornerFrom;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.START_PLAYING;
import static de.amr.games.pacman.model.PacManGame.FSM_LOGGER;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.theme.PacManTheme.MAZE_FLASH_TIME_MILLIS;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The Pac-Man game controller (finite state machine).
 * 
 * @author Armin Reichert
 */
public class PacManGameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	private PacManGame game;
	private PacManTheme theme;
	private Controller currentView;
	private PacManGameCast cast;
	private GhostMotionTimer ghostMotionTimer;
	private PlayingState playingState;
	private IntroView introView;
	private PlayView playView;
	public boolean globalDotCounterEnabled;

	public PacManGameController(PacManTheme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		buildStateMachine();
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		traceTo(PacManGame.FSM_LOGGER, () -> 60);
	}

	// The finite state machine

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						introView = new IntroView(theme, app().settings.width, app().settings.height);
						show(introView);
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						game.init();
						theme.snd_clips_all().forEach(Sound::stop);
						theme.snd_ready().play();
						cast.actors().forEach(cast::putOnStage);
						playView.init();
						playView.textColor = Color.YELLOW;
						playView.message = "Ready!";
						playView.setShowScores(true);
						globalDotCounterEnabled = false;
						game.globalDotCounter = 0;
					})
					.onTick(() -> {
						cast.ghostsOnStage().forEach(Ghost::update);
					})
				
				.state(START_PLAYING)
					.timeoutAfter(sec(1.7f))
					.onEntry(() -> {
						ghostMotionTimer.init();
						cast.ghosts().forEach(ghost -> ghost.dotCounter = 0);
						theme.music_playing().volume(.90f);
						theme.music_playing().loop();
						playView.message = null;
						playView.energizerBlinking.setEnabled(true);
					})
					.onTick(() -> {
						cast.ghostsOnStage().forEach(Ghost::update);
					})
				
				.state(PLAYING)
					.impl(playingState = new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(4 + game.level.mazeNumFlashes * MAZE_FLASH_TIME_MILLIS / 1000))
					.onEntry(() -> {
						theme.snd_clips_all().forEach(Sound::stop);
						cast.pacMan.sprites.select("full");
					})
					.onTick(() -> {
						if (state().getTicksConsumed() == sec(2)) {
							cast.ghostsOnStage().forEach(Ghost::hide);
							playView.mazeFlashing = game.level.mazeNumFlashes > 0;
						}
						else if (state().getTicksRemaining() == sec(2)) {
							game.enterLevel(game.level.number + 1);
							cast.actorsOnStage().forEach(MazeResident::init);
							playView.init(); // stops flashing
							resetGhostDotCounters();
						} 
						else if (state().getTicksRemaining() < sec(1.8f)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
					.onExit(() -> {
						LOGGER.info(() -> String.format("Ghosts killed in level %d: %d", game.level.number, game.level.ghostKilledInLevel));
					})
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(sec(1))
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(6) : sec(4))
					.onEntry(() -> {
						theme.snd_clips_all().forEach(Sound::stop);
						game.lives -= app().settings.getAsBoolean("pacMan.immortable") ? 0 : 1;
					})
					.onTick(() -> {
						int passedTime = state().getTicksConsumed();
						// wait first 1.5 sec before starting the "dying" animation
						if (passedTime == sec(1.5f)) {
							cast.ghostsOnStage().forEach(Ghost::hide);
							cast.removeBonus();
							theme.snd_die().play();
							cast.pacMan.sprites.select("dying");
						}
						// run "dying" animation
						if (passedTime > sec(1.5f) && passedTime < sec(2)) {
							cast.pacMan.update();
						}
						// inform Pac-Man that he died
						if (passedTime == sec(2)) {
							cast.pacMan.process(new PacManDiedEvent());
						}
						if (game.lives == 0) {
							return;
						}
						// if playing continues, init actors and view
						if (passedTime == sec(4)) {
							cast.actorsOnStage().forEach(MazeResident::init);
							theme.music_playing().loop();
							playView.init();
						}
						// let ghosts jump a bit before game play continues
						if (passedTime > sec(4)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						game.saveHiscore();
						cast.ghostsOnStage().forEach(Ghost::show);
						cast.removeBonus();
						theme.music_gameover().play();
						playView.enableAnimations(false);
						playView.textColor = Color.RED;
						playView.message = "Game   Over!";
						
					})
					.onExit(() -> {
						theme.music_gameover().stop();
						playView.message = null;
					})

			.transitions()
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
					.act(this::createPlayingStage)
				
				.when(GETTING_READY).then(START_PLAYING)
					.onTimeout()
				
				.when(START_PLAYING).then(PLAYING)
					.onTimeout()
					
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState::onFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(playingState::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(playingState::onPacManGhostCollision)
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(playingState::onPacManGainsPower)
					
				.stay(PLAYING)
					.on(PacManGettingWeakerEvent.class)
					.act(playingState::onPacManGettingWeaker)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(playingState::onPacManLostPower)
			
				.when(PLAYING).then(GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(playingState::onGhostKilled)
					
				.when(PLAYING).then(PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(playingState::onPacManKilled)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
					.act(() -> ghostMotionTimer.init())
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(() -> ghostMotionTimer.init())
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !theme.music_gameover().isRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	// Classes implementing the FSM states:

	/**
	 * "Playing" state implementation.
	 */
	private class PlayingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			cast.ghostsOnStage().forEach(Ghost::show);
			playView.init();
			playView.enableAnimations(true);
			playView.energizerBlinking.setEnabled(true);
		}

		@Override
		public void onTick() {
			ghostMotionTimer.update();
			cast.pacMan.update();
			cast.bonus().ifPresent(Bonus::update);
			cast.ghosts().forEach(ghost -> ghost.nextState = ghostMotionTimer.getState());
			Iterable<Ghost> ghosts = cast.ghostsOnStage()::iterator;
			for (Ghost ghost : ghosts) {
				if (ghost.is(LOCKED) && canLeaveHouse(ghost, cast.game.level.number)) {
					ghost.process(new GhostUnlockedEvent());
				} else if (ghost.is(CHASING) && ghostMotionTimer.is(SCATTERING)) {
					ghost.process(new StartScatteringEvent());
				} else if (ghost.is(SCATTERING) && ghostMotionTimer.is(CHASING)) {
					ghost.process(new StartChasingEvent());
				} else {
					ghost.update();
				}
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			if (e.ghost.is(CHASING, SCATTERING)) {
				enqueue(new PacManKilledEvent(e.ghost));
			} else if (e.ghost.is(FRIGHTENED)) {
				enqueue(new GhostKilledEvent(e.ghost));
			}
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.name(), e.killer.tile()));
			theme.music_playing().stop();
			cast.pacMan.process(event);
			playView.energizerBlinking.setEnabled(false);
			enableGlobalDotCounter();
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			ghostMotionTimer.suspend();
			cast.ghostsOnStage().forEach(ghost -> ghost.process(event));
			cast.pacMan.process(event);
		}

		private void onPacManGettingWeaker(PacManGameEvent event) {
			cast.ghostsOnStage().forEach(ghost -> ghost.process(event));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostMotionTimer.resume();
			cast.ghostsOnStage().forEach(ghost -> ghost.process(event));
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.name(), e.ghost.tile()));
			theme.snd_eatGhost().play();
			e.ghost.process(event);
		}

		private void onBonusFound(PacManGameEvent event) {
			cast.bonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found %s and wins %d points", bonus.symbol, bonus.value));
				int livesBeforeScoring = game.lives;
				game.score(bonus.value);
				theme.snd_eatFruit().play();
				if (game.lives > livesBeforeScoring) {
					theme.snd_extraLife().play();
				}
				bonus.process(event);
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			int points = game.eatFoodAt(e.tile);
			int livesBeforeScoring = game.lives;
			game.score(points);
			updateDotCounters();
			theme.snd_eatPill().play();
			if (game.lives > livesBeforeScoring) {
				theme.snd_extraLife().play();
			}
			if (game.numPelletsRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusScoreReached()) {
				cast.addBonus();
				cast.bonus().ifPresent(bonus -> {
					LOGGER.info(() -> String.format("Bonus %s added, time: %.2f sec", bonus, bonus.state().getDuration() / 60f));
				});
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	/**
	 * "Ghost dying" state implementation.
	 */
	private class GhostDyingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			cast.pacMan.hide();
			game.level.ghostsKilledByEnergizer += 1;
			game.level.ghostKilledInLevel += 1;
			int points = 100 * (int) Math.pow(2, game.level.ghostsKilledByEnergizer);
			int livesBefore = game.lives;
			game.score(points);
			if (game.lives > livesBefore) {
				theme.snd_extraLife().play();
			}
			if (game.level.ghostKilledInLevel == 16) {
				game.score(12000);
			}
			LOGGER.info(() -> String.format("Scored %d points for killing %s ghost in sequence", points,
					new String[] { "", "first", "2nd", "3rd", "4th" }[game.level.ghostsKilledByEnergizer]));
		}

		@Override
		public void onTick() {
			cast.bonus().ifPresent(Bonus::update);
			cast.ghostsOnStage().filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE)).forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			cast.pacMan.show();
		}
	}

	// View handling

	private void show(Controller view) {
		if (this.currentView != view) {
			this.currentView = view;
			view.init();
		}
	}

	@Override
	public View currentView() {
		return (View) currentView;
	}

	// Controller methods

	private void createPlayingStage() {
		game = new PacManGame();
		ghostMotionTimer = new GhostMotionTimer(game);
		cast = new PacManGameCast(game, theme);
		cast.pacMan.addGameEventListener(this::process);
		playView = new PlayView(cast);
		playView.fnGhostMotionState = ghostMotionTimer::state;
		show(playView);
	}

	@Override
	public void update() {
		handlePlayingSpeedChange();
		handleToggleStateMachineLogging();
		handleToggleGhostFrightenedBehavior();
		handleToggleOverflowBug();
		handleCheats();
		super.update();
		currentView.update();
	}

	// Input

	private void handleCheats() {
		/* ALT-"K": Kill all ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			cast.ghostsOnStage().forEach(ghost -> ghost.process(new GhostKilledEvent(ghost)));
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			game.maze.tiles().filter(Tile::containsPellet).forEach(tile -> {
				game.eatFoodAt(tile);
				updateDotCounters();
			});
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (is(PLAYING)) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.level.number + 1));
				enqueue(new LevelCompletedEvent());
			}
		}
		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			boolean immortable = app().settings.getAsBoolean("pacMan.immortable");
			app().settings.set("pacMan.immortable", !immortable);
			LOGGER.info("Pac-Man immortable = " + app().settings.getAsBoolean("pacMan.immortable"));
		}
	}

	private void handleToggleOverflowBug() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_O)) {
			app().settings.set("overflowBug", !app().settings.getAsBoolean("overflowBug"));
			LOGGER.info("Overflow bug is " + (app().settings.getAsBoolean("overflowBug") ? "on" : "off"));
		}
	}

	private void handleToggleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			FSM_LOGGER.setLevel(FSM_LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + FSM_LOGGER.getLevel());
		}
	}

	private void handlePlayingSpeedChange() {
		int fps = app().clock.getFrequency();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			setClockFrequency(PacManGame.SPEED_1_FPS);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			setClockFrequency(PacManGame.SPEED_2_FPS);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			setClockFrequency(PacManGame.SPEED_3_FPS);
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_LEFT)) {
			setClockFrequency(fps <= 10 ? Math.max(1, fps - 1) : fps - 5);
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_RIGHT)) {
			setClockFrequency(fps < 10 ? fps + 1 : fps + 5);
		}
	}

	private void setClockFrequency(int ticksPerSecond) {
		app().clock.setFrequency(ticksPerSecond);
		LOGGER.info(() -> String.format("Clock frequency set to %d ticks/sec", ticksPerSecond));
	}

	private void handleToggleGhostFrightenedBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			boolean original = app().settings.getAsBoolean("ghost.originalBehavior");
			if (original) {
				app().settings.set("ghost.originalBehavior", false);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isFleeingToSafeCornerFrom(cast.pacMan)));
				LOGGER.info(() -> "Changed ghost escape behavior to escaping via safe route");
			} else {
				app().settings.set("ghost.originalBehavior", true);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isMovingRandomlyWithoutTurningBack()));
				LOGGER.info(() -> "Changed ghost escape behavior to original random movement");
			}
		}
	}

	// Ghost house rules

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost       a ghost
	 * @param levelNumber the level number
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private boolean canLeaveHouse(Ghost ghost, int levelNumber) {
		if (ghost == cast.blinky) {
			LOGGER.info(() -> "Blinky can always leave house immediatley");
			return true;
		}
		Optional<Ghost> nextGhostToLeaveHouse = preferredLockedGhost();
		if (!nextGhostToLeaveHouse.isPresent() || nextGhostToLeaveHouse.get() != ghost) {
			return false;
		}
		int ghostDotLimit = ghostDotLimit(ghost, levelNumber);
		if (ghost.dotCounter >= ghostDotLimit) {
			LOGGER
					.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(), ghostDotLimit));
			return true;
		}
		if (globalDotCounterEnabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (game.globalDotCounter >= globalDotLimit) {
				LOGGER.info(
						() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(), globalDotLimit));
				return true;
			}
		}
		int timeout = levelNumber < 5 ? sec(4) : sec(3);
		if (cast.pacMan.ticksSinceLastMeal > timeout) {
			LOGGER.info(
					() -> String.format("%s can leave house: Pac-Man's eat timeout (%d ticks) reached", ghost.name(), timeout));
			return true;
		}
		return false;
	}

	private Optional<Ghost> preferredLockedGhost() {
		return Stream.of(cast.pinky, cast.inky, cast.clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private void enableGlobalDotCounter() {
		globalDotCounterEnabled = true;
		game.globalDotCounter = 0;
		LOGGER.info(() -> "Global dot counter enabled and set to zero");
	}

	private void resetGhostDotCounters() {
		cast.ghosts().forEach(ghost -> ghost.dotCounter = 0);
		LOGGER.info(() -> "Ghost dot counters enabled and set to zero");
	}

	private void updateDotCounters() {
		if (globalDotCounterEnabled) {
			game.globalDotCounter++;
			LOGGER.fine(() -> String.format("Global dot counter: %d", game.globalDotCounter));
			if (game.globalDotCounter == 32 && cast.clyde.is(LOCKED)) {
				globalDotCounterEnabled = false;
				game.globalDotCounter = 0;
				LOGGER.info(() -> "Global dot counter reset to zero");
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghost.dotCounter++;
				LOGGER.fine(() -> String.format("%s's dot counter: %d", ghost.name(), ghost.dotCounter));
			});
		}
	}

	private int ghostDotLimit(Ghost ghost, int levelNumber) {
		if (ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return levelNumber == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private int globalDotLimit(Ghost ghost) {
		return (ghost == cast.pinky) ? 7 : (ghost == cast.inky) ? 17 : (ghost == cast.clyde) ? 32 : 0;
	}
}