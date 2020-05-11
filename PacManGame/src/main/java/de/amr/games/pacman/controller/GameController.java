package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.LOADING_MUSIC;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.model.Timing.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MovingActor;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.PacManGameView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.LoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	private Game game;

	private Theme theme;
	private SoundController sound;

	private LoadingView loadingView;
	private IntroView introView;
	private PlayView playView;
	private PacManGameView currentView;

	private GhostCommand ghostCommand;
	private GhostHouse ghostHouse;

	private boolean showFrameRate;
	private boolean showRoutes;
	private boolean showStates;
	private boolean showGrid;

	public GameController(Theme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		loadingView = new LoadingView(theme);
		introView = new IntroView(theme);
		sound = new SoundController(theme);
		buildStateMachine();
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		getTracer().setLogger(PacManStateMachineLogging.LOG);
		doNotLogEventProcessingIf(PacManGameEvent::isTrivial);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	private void showView(PacManGameView view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	private void createPlayEnvironment() {
		game = new Game();
		game.movingActors().forEach(actor -> {
			game.stage.add(actor);
			actor.addEventListener(this::process);
		});
		ghostCommand = new GhostCommand(game);
		ghostHouse = new GhostHouse(game);
		playView = new PlayView(game, theme);
		playView.fnGhostCommandState = ghostCommand::state;
		playView.house = ghostHouse;
		playView.showFPS = () -> showFrameRate;
		playView.showGrid = () -> showGrid;
		playView.showRoutes = () -> showRoutes;
		playView.showStates = () -> showStates;
	}

	public void onExit() {
		if (game != null) {
			game.hiscore.save();
		}
	}

	@Override
	public void update() {
		if (currentView == playView) {
			if (Keyboard.keyPressedOnce("b")) {
				toggleGhostActing(game.blinky);
			} else if (Keyboard.keyPressedOnce("c")) {
				toggleGhostActing(game.clyde);
			} else if (Keyboard.keyPressedOnce("d")) {
				toggleDemoMode();
			} else if (Keyboard.keyPressedOnce("e")) {
				eatAllSimplePellets();
			} else if (Keyboard.keyPressedOnce("f")) {
				toggleGhostFrightenedBehavior();
			} else if (Keyboard.keyPressedOnce("g")) {
				showGrid = !showGrid;
			} else if (Keyboard.keyPressedOnce("i")) {
				toggleGhostActing(game.inky);
			} else if (Keyboard.keyPressedOnce("k")) {
				killAllGhosts();
			} else if (Keyboard.keyPressedOnce("l")) {
				toggleStateMachineLogging();
			} else if (Keyboard.keyPressedOnce("m")) {
				toggleMakePacManImmortable();
			} else if (Keyboard.keyPressedOnce("o")) {
				togglePacManOverflowBug();
			} else if (Keyboard.keyPressedOnce("p")) {
				toggleGhostActing(game.pinky);
			} else if (Keyboard.keyPressedOnce("s")) {
				showStates = !showStates;
			} else if (Keyboard.keyPressedOnce("t")) {
				showFrameRate = !showFrameRate;
			} else if (Keyboard.keyPressedOnce("r")) {
				showRoutes = !showRoutes;
			} else if (Keyboard.keyPressedOnce("+")) {
				switchToNextLevel();
			}
		}

		if (Keyboard.keyPressedOnce("1") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			changeClockFrequency(Game.SPEED_1_FPS);
		} else if (Keyboard.keyPressedOnce("2") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			changeClockFrequency(Game.SPEED_2_FPS);
		} else if (Keyboard.keyPressedOnce("3") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			changeClockFrequency(Game.SPEED_3_FPS);
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_LEFT)) {
			int oldFreq = app().clock().getTargetFramerate();
			changeClockFrequency(oldFreq <= 10 ? Math.max(1, oldFreq - 1) : oldFreq - 5);
		} else if (Keyboard.keyPressedOnce(Modifier.CONTROL, KeyEvent.VK_RIGHT)) {
			int oldFreq = app().clock().getTargetFramerate();
			changeClockFrequency(oldFreq < 10 ? oldFreq + 1 : oldFreq + 5);
		}

		if (eventQ().size() >= 2) {
			PacManStateMachineLogging.LOG.warning("Event queue contains more than one element");
		}
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
						showView(loadingView);
					})
					
				.state(INTRO)
					.onEntry(() -> {
						showView(introView);
					})
					.onExit(() -> {
						sound.muteAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						createPlayEnvironment();
						showView(playView);
						sound.gameReady();
						setDemoMode(settings.demoMode);
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.message.color = Color.YELLOW;
							playView.message.text = "Ready!";
							playView.startEnergizerBlinking();
							sound.gameStarts();
						}
						game.movingActorsOnStage().forEach(MovingActor::update);
					})
					.onExit(() -> {
						playView.message.text = "";
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(playView.mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						game.pacMan.sprites.select("full");
						ghostHouse.onLevelChange();
						sound.muteSoundEffects();
						playView.enableGhostAnimations(false);
						playView.stopEnergizerBlinking();
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilledInLevel);
					})
					.onTick((state, t, remaining) -> {
						float f = playView.mazeFlashingSeconds();

						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							game.ghostsOnStage().forEach(ghost -> ghost.visible = false);
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
							game.enterLevel(game.level.number + 1);
							game.movingActorsOnStage().forEach(MovingActor::init);
							playView.init();
						}
						
						// After two more seconds, enable ghost animations again
						if (t == sec(6 + f)) {
							playView.enableGhostAnimations(true);
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (t >= sec(6 + f)) {
							game.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						game.pacMan.visible = false;
					})
					.onTick(() -> {
						game.bonus.update();
						game.ghostsOnStage()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						game.pacMan.visible = true;
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
							game.pacMan.sprites.select("full");
							game.bonus.hide();
							game.ghostsOnStage().forEach(ghost -> ghost.visible = false);
						}
						else if (t == sec(3)) {
							// start the "dying" animation
							game.pacMan.sprites.select("dying");
							sound.pacManDied();
						}
						else if (t == sec(7) - 1 && game.lives > 0) {
							// initialize actors and view, continue game
							game.movingActorsOnStage().forEach(MovingActor::init);
							playView.init();
							sound.gameStarts();
						}
						else if (t > sec(7)) {
							// let ghosts jump a bit while music is starting
							game.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						game.hiscore.save();
						game.ghostsOnStage().forEach(ghost -> ghost.visible = true);
						playView.enableGhostAnimations(false);
						playView.message.color = Color.RED;
						playView.message.text = "Game   Over!";
						sound.gameOver();
					})
					.onExit(() -> {
						playView.message.text = "";
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
					.act(playingState()::reset)
				
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState()::onFoodFound)
					
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
					.act(playingState()::reset)
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(playingState()::reset)
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(" "))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sound.isGameOverMusicRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState> {

		@Override
		public void onTick() {
			ghostCommand.update();
			ghostHouse.update();
			game.movingActorsOnStage().forEach(MovingActor::update);
			game.bonus.update();
			sound.updatePlayingSounds(game);
		}

		@Override
		public void onExit() {
			sound.muteGhostSounds();
		}

		private void reset() {
			ghostCommand.init();
			game.ghostsOnStage().forEach(ghost -> ghost.visible = true);
			game.pacMan.setState(PacManState.EATING);
			playView.init();
			playView.enableGhostAnimations(true);
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
				game.scoreKilledGhost(ghost.name);
				if (game.lives > livesBefore) {
					sound.extraLife();
				}
				sound.ghostEaten();
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("Ghost %s killed at %s", ghost.name, ghost.tile());
			} else {
				// Pac-Man killed
				ghostHouse.onLifeLost();
				sound.muteAll();
				playView.stopEnergizerBlinking();
				game.pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tile());
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			loginfo("PacMan found %s and wins %d points", game.bonus.symbol(), game.bonus.value());
			int livesBefore = game.lives;
			game.score(game.bonus.value());
			sound.bonusEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
			}
			game.bonus.process(event);
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			boolean energizer = game.maze.isEnergizer(found.tile);
			ghostHouse.onPacManFoundFood(found);
			int points = game.eatFood(found.tile);
			int livesBefore = game.lives;
			game.score(points);
			sound.pelletEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
			}
			if (game.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusScoreReached()) {
				game.bonus.show(theme);
				loginfo("Bonus %s added, time: %.2f sec", game.bonus, game.bonus.state().getDuration() / 60f);
			}
			if (energizer) {
				ghostCommand.suspend();
				sound.pacManGainsPower();
				game.pacMan.powerTicks = sec(game.level.pacManPowerSeconds);
				game.ghostsOnStage().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
			}
		}
	}

	private void changeClockFrequency(int newValue) {
		if (app().clock().getTargetFramerate() != newValue) {
			app().clock().setTargetFramerate(newValue);
			loginfo("Clock frequency changed to %d ticks/sec", newValue);
		}
	}

	private void togglePacManOverflowBug() {
		settings.overflowBug = !settings.overflowBug;
		loginfo("Overflow bug is %s", (settings.overflowBug ? "on" : "off"));
	}

	private void toggleStateMachineLogging() {
		PacManStateMachineLogging.toggle();
		loginfo("State machine logging changed to %s", PacManStateMachineLogging.LOG.getLevel());
	}

	private void toggleGhostFrightenedBehavior() {
		if (settings.ghostsFleeRandomly) {
			settings.ghostsFleeRandomly = false;
			game.ghosts().forEach(ghost -> ghost.behavior(FRIGHTENED, ghost.isFleeingToSafeCorner(game.pacMan)));
			loginfo("Changed ghost escape behavior to escaping via safe route");
		} else {
			settings.ghostsFleeRandomly = true;
			game.ghosts().forEach(ghost -> ghost.behavior(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack()));
			loginfo("Changed ghost escape behavior to original random movement");
		}
	}

	private void toggleDemoMode() {
		settings.demoMode = !settings.demoMode;
		setDemoMode(settings.demoMode);
		loginfo("Demo mode is %s", (settings.demoMode ? "on" : "off"));
	}

	private void setDemoMode(boolean on) {
		if (on) {
			settings.pacManImmortable = true;
			game.pacMan.behavior(game.pacMan.isMovingRandomlyWithoutTurningBack());
		} else {
			settings.pacManImmortable = false;
			game.pacMan.behavior(game.pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
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
		game.maze.playingArea().filter(game.maze::isSimplePellet).forEach(tile -> {
			game.eatFood(tile);
			ghostHouse.onPacManFoundFood(new FoodFoundEvent(tile));
			ghostHouse.update();
		});
		loginfo("All simple pellets eaten");
	}

	private void toggleGhostActing(Ghost ghost) {
		if (game.stage.contains(ghost)) {
			game.stage.remove(ghost);
		} else {
			game.stage.add(ghost);
		}
	}

	private void killAllGhosts() {
		game.level.ghostsKilledByEnergizer = 0;
		game.ghostsOnStage().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
			game.scoreKilledGhost(ghost.name);
			ghost.process(new GhostKilledEvent(ghost));
		});
		loginfo("All ghosts killed");
	}
}