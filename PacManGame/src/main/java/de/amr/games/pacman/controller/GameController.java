package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.LOADING_MUSIC;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.model.Game.FSM_LOGGER;
import static de.amr.games.pacman.model.Timing.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.logging.Level;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.GameView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.LoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	private Game game;
	private Theme theme;
	private Cast cast;
	private GhostCommand ghostCommand;
	private House house;
	private Cheats cheats;
	private SoundController sound;

	private GameView currentView;
	private LoadingView loadingView;
	private IntroView introView;
	private PlayView playView;

	private boolean showFPS;
	private boolean showRoutes;
	private boolean showStates;
	private boolean showGrid;

	public GameController(Theme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		sound = new SoundController(theme);
		buildStateMachine();
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		setLogger(Game.FSM_LOGGER);
		doNotLogEventProcessingIf(PacManGameEvent::isTrivial);
	}

	public Optional<Cast> cast() {
		return Optional.ofNullable(cast);
	}

	public Optional<House> ghostHouse() {
		return Optional.ofNullable(house);
	}

	public Optional<Game> game() {
		return Optional.ofNullable(game);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	private void showView(GameView view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	private void createPlayingEnvironment() {
		game = new Game();
		cast = new Cast(game, theme);
		cast.actors().forEach(actor -> {
			cast.setActorOnStage(actor);
			actor.addEventListener(this::process);
		});
		cast.bonus.init();
		enterDemoMode(settings.demoMode);
		ghostCommand = new GhostCommand(cast);
		house = new House(cast);
		cheats = new Cheats(this);
		createPlayView();
		showView(playView);
	}

	private void enterDemoMode(boolean on) {
		if (on) {
			settings.pacManImmortable = true;
			cast.pacMan.steering(cast.pacMan.isMovingRandomlyWithoutTurningBack());
		} else {
			settings.pacManImmortable = false;
			cast.pacMan.steering(cast.pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
		LOGGER.info("Demo mode = " + on);
	}

	private void createPlayView() {
		playView = new PlayView(cast);
		playView.fnGhostCommandState = ghostCommand::state;
		playView.house = house;
		playView.showFPS = () -> showFPS;
		playView.showGrid = () -> showGrid;
		playView.showRoutes = () -> showRoutes;
		playView.showStates = () -> showStates;
	}

	@Override
	public void update() {
		handleChangeStateMachineLogging();
		handleChangeGhostFrightenedBehavior();
		handleChangePacManOverflowBug();
		handleChangeClockSpeed();
		handleChangePlayViewSettings();
		handleChangeDemoMode();
		super.update();
		currentView.update();
	}

	private PlayingState playingState() {
		return state(PLAYING);
	}

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(LOADING_MUSIC)
			
			.states()
			
				.state(LOADING_MUSIC)
					.onEntry(() -> {
						sound.loadMusic();
						loadingView = new LoadingView(theme);
						showView(loadingView);
					})
					
				.state(INTRO)
					.onEntry(() -> {
						introView = new IntroView(theme);
						showView(introView);
					})
					.onExit(() -> {
						sound.muteAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						createPlayingEnvironment();
						sound.gameReady();
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.messageColor(Color.YELLOW);
							playView.message("Ready!");
							playView.startEnergizerBlinking();
							sound.gameStarts();
						}
						cast.actorsOnStage().forEach(Actor::update);
					})
					.onExit(() -> {
						playView.clearMessage();
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(playView.mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						cast.pacMan.sprites.select("full");
						cast.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(false));
						house.onLevelChange();
						sound.muteSoundEffects();
						playView.stopEnergizerBlinking();
						LOGGER.info(() -> String.format("Ghosts killed in level %d: %d", 
								game.level().number, game.level().ghostsKilledInLevel));
					})
					.onTick((state, t, remaining) -> {
						float f = playView.mazeFlashingSeconds();
						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							cast.ghostsOnStage().forEach(ghost -> ghost.setVisible(false));
							if (f > 0) {
								playView.showFlashingMaze();
							}
						}

						// After flashing, show empty maze.
						if (t == sec(2 + f)) {
							playView.showEmptyMaze();
						}
						
						// After two more seconds, change level and show crowded maze.
						if (t == sec(4 + f)) {
							game.enterLevel(game.level().number + 1);
							cast.actorsOnStage().forEach(Actor::init);
							playView.init();
						}
						
						// After two more seconds, enable ghosts again
						if (t == sec(6 + f)) {
							cast.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(true));
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (t >= sec(6 + f)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						cast.pacMan.setVisible(false);
					})
					.onTick(() -> {
						cast.bonus.update();
						cast.ghostsOnStage()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						cast.pacMan.setVisible(true);
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(9) : sec(7))
					.onEntry(() -> {
						game.lives -= settings.pacManImmortable ? 0 : 1;
						sound.muteSoundEffects();
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(1)) {
							// Pac-Man stops struggling
							cast.pacMan.sprites.select("full");
							cast.hideBonus();
							cast.ghostsOnStage().forEach(ghost -> ghost.setVisible(false));
						}
						else if (t == sec(3)) {
							// start the "dying" animation
							cast.pacMan.sprites.select("dying");
							sound.pacManDied();
						}
						else if (t == sec(7) - 1) {
							if (game.lives > 0) {
								// initialize actors and view for continuing game
								cast.actorsOnStage().forEach(Actor::init);
								playView.init();
								sound.gameStarts();
							}
						}
						else if (t > sec(7)) {
							// let ghosts jump a bit while music is starting
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						game.saveHiscore();
						cast.ghostsOnStage().forEach(ghost -> ghost.setVisible(true));
						playView.disableAnimations();
						playView.messageColor(Color.RED);
						playView.message("Game   Over!");
						sound.gameOver();
					})
					.onExit(() -> {
						playView.clearMessage();
						sound.muteAll();
					})

			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> sound.isMusicLoadingComplete()	&& settings.skipIntro)

				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> sound.isMusicLoadingComplete())
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete())
				
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.act(() -> playingState().reset())
				
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState()::onPacManFoundFood)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(playingState()::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(playingState()::onPacManLostPower)
			
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(playingState()::onPacManGhostCollision)
			
				.when(PLAYING).then(PACMAN_DYING)	
					.on(PacManKilledEvent.class)

				.when(PLAYING).then(GHOST_DYING)	
					.on(GhostKilledEvent.class)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
					.act(() -> {
						playingState().reset();
					})
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(() -> {
						playingState().reset();
					})
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sound.isGameOverMusicRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onTick() {
			ghostCommand.update();
			cheats.update();
			house.update();
			cast.actorsOnStage().forEach(Actor::update);
			cast.bonus.update();
			sound.updatePlayingSounds(cast);
		}

		@Override
		public void onExit() {
			sound.muteGhostSounds();
		}

		private void reset() {
			ghostCommand.init();
			cast.ghostsOnStage().forEach(ghost -> ghost.setVisible(true));
			cast.pacMan.startEating();
			playView.init();
			playView.enableAnimations();
			playView.startEnergizerBlinking();
		}

		private void onPacManLostPower(PacManGameEvent event) {
			sound.pacManLostPower();
			ghostCommand.resume();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			if (ghost.is(FRIGHTENED)) {
				// Ghost killed
				int livesBefore = game.lives;
				game.scoreKilledGhost(ghost.name());
				if (game.lives > livesBefore) {
					sound.extraLife();
				}
				sound.ghostEaten();
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				LOGGER.info(() -> String.format("Ghost %s killed at %s", ghost.name(), ghost.tile()));
			} else {
				// Pac-Man killed
				house.onLifeLost();
				sound.muteAll();
				playView.stopEnergizerBlinking();
				cast.pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				LOGGER.info(() -> String.format("Pac-Man killed by %s at %s", ghost.name(), ghost.tile()));
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			LOGGER.info(() -> String.format("PacMan found %s and wins %d points", cast.bonus.symbol(), cast.bonus.value()));
			int livesBefore = game.lives;
			game.score(cast.bonus.value());
			sound.bonusEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
			}
			cast.bonus.process(event);
		}

		private void onPacManFoundFood(PacManGameEvent event) {
			FoodFoundEvent foodFound = (FoodFoundEvent) event;
			house.onPacManFoundFood(foodFound);
			int points = game.eatFoodAt(foodFound.tile);
			int livesBefore = game.lives;
			game.score(points);
			sound.pelletEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
			}
			if (game.numPelletsRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusScoreReached()) {
				cast.showBonus();
				LOGGER.info(
						() -> String.format("Bonus %s added, time: %.2f sec", cast.bonus, cast.bonus.state().getDuration() / 60f));
			}
			if (foodFound.energizer) {
				ghostCommand.suspend();
				sound.pacManGainsPower();
				cast.pacMan.gainPower();
			}
		}
	}

	// handle input

	private void handleChangeClockSpeed() {
		int oldFreq = app().clock().getFrequency();
		int newFreq = oldFreq;
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			newFreq = Game.SPEED_1_FPS;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			newFreq = Game.SPEED_2_FPS;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			newFreq = Game.SPEED_3_FPS;
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_LEFT)) {
			newFreq = (oldFreq <= 10 ? Math.max(1, oldFreq - 1) : oldFreq - 5);
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_RIGHT)) {
			newFreq = (oldFreq < 10 ? oldFreq + 1 : oldFreq + 5);
		}
		if (newFreq != oldFreq) {
			app().clock().setFrequency(newFreq);
			LOGGER.info(String.format("Clock frequency changed to %d ticks/sec", newFreq));
		}
	}

	private void handleChangePlayViewSettings() {
		if (currentView != playView) {
			return;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_T)) {
			showFPS = !showFPS;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_G)) {
			showGrid = !showGrid;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_S)) {
			showStates = !showStates;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			showRoutes = !showRoutes;
		}
	}

	private void handleChangePacManOverflowBug() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_O)) {
			settings.overflowBug = !settings.overflowBug;
			LOGGER.info("Overflow bug is " + (settings.overflowBug ? "on" : "off"));
		}
	}

	private void handleChangeStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			FSM_LOGGER.setLevel(FSM_LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + FSM_LOGGER.getLevel());
		}
	}

	private void handleChangeGhostFrightenedBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			boolean original = settings.ghostsFleeRandomly;
			if (original) {
				settings.ghostsFleeRandomly = false;
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, ghost.isFleeingToSafeCorner(cast.pacMan)));
				LOGGER.info(() -> "Changed ghost escape behavior to escaping via safe route");
			} else {
				settings.ghostsFleeRandomly = true;
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack()));
				LOGGER.info(() -> "Changed ghost escape behavior to original random movement");
			}
		}
	}

	private void handleChangeDemoMode() {
		/* CONTROL-"J": Demo mode: Makes Pac-Man immortable and moving randomly. */
		if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_J) && cast != null) {
			settings.demoMode = !settings.demoMode;
			enterDemoMode(settings.demoMode);
		}
	}
}