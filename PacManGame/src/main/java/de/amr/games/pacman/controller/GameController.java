package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.Steerings.isFleeingToSafeCornerFrom;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;
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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.actor.core.Actor;
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
	private CompletableFuture<Void> musicLoading;

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
		ghostCommand = new GhostCommand(cast);
		house = new House(cast);
		cheats = new Cheats(this);
		createPlayView();
		showView(playView);
	}

	private void createPlayView() {
		playView = new PlayView(cast);
		playView.fnGhostCommandState = ghostCommand::state;
		playView.ghostHouse = house;
		playView.showFPS = () -> showFPS;
		playView.showGrid = () -> showGrid;
		playView.showRoutes = () -> showRoutes;
		playView.showStates = () -> showStates;
	}

	@Override
	public void update() {
		onChangeStateMachineLogging();
		onChangeGhostFrightenedBehavior();
		onChangePacManOverflowBug();
		onChangeClockSpeed();
		onChangePlayViewSettings();
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
						loadMusic();
						loadingView = new LoadingView(theme);
						showView(loadingView);
					})
					
				.state(INTRO)
					.onEntry(() -> {
						introView = new IntroView(theme);
						showView(introView);
					})
					.onExit(() -> {
						stopSoundEffects();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						createPlayingEnvironment();
						playSoundReady();
					})
					.onTick(() -> {
						int t = state().getTicksConsumed();
						if (t == sec(5)) {
							playView.messageColor(Color.YELLOW);
							playView.message("Ready!");
							playView.startEnergizerBlinking();
							loopMusicPlaying();
						}
						cast.actorsOnStage().forEach(Actor::update);
					})
					.onExit(() -> {
						playView.clearMessage();
						ghostCommand.init();
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(4 + playView.mazeFlashingSeconds()))
					.onEntry(() -> {
						cast.pacMan.sprites.select("full");
						house.resetGhostDotCounters();
						stopSoundEffects();
						
					})
					.onTick(() -> {
						int t = state().getTicksConsumed();
						if (t == sec(2)) {
							cast.ghostsOnStage().forEach(Ghost::hide);
							if (game.level().mazeNumFlashes > 0) {
								playView.startMazeFlashing();
							}
						}
						if (t == sec(2 + playView.mazeFlashingSeconds())) {
							LOGGER.info(() -> String.format("Ghosts killed in level %d: %d", 
									game.level().number, game.level().ghostsKilledInLevel));
							game.enterLevel(game.level().number + 1);
							cast.actorsOnStage().forEach(Actor::init);
							playView.init(); // stops flashing
						} 
						if (t == sec(4)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						cast.pacMan.hide();
					})
					.onTick(() -> {
						cast.bonus().ifPresent(Bonus::update);
						cast.ghostsOnStage()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						cast.pacMan.show();
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(9) : sec(7))
					.onEntry(() -> {
						game.lives -= app().settings.getAsBoolean("PacMan.immortable") ? 0 : 1;
						stopSoundEffects();
					})
					.onTick(() -> {
						int t = state().getTicksConsumed();
						if (t == sec(1)) {
							// Pac-Man stops struggling
							cast.pacMan.sprites.current().get().enableAnimation(false);
							cast.pacMan.sprites.select("full");
							cast.removeBonus();
							cast.ghostsOnStage().forEach(Ghost::hide);
						}
						else if (t == sec(4)) {
							// start the "dying" animation
							cast.pacMan.sprites.select("dying");
							playSoundPacManDied();
						}
						else if (t == sec(7)) {
							if (game.lives > 0) {
								// initialize actors and view for continuing game
								cast.actorsOnStage().forEach(Actor::init);
								playView.init();
								loopMusicPlaying();
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
						cast.ghostsOnStage().forEach(Ghost::show);
						playView.disableAnimations();
						playView.messageColor(Color.RED);
						playView.message("Game   Over!");
						playSoundGameOver();
					})
					.onExit(() -> {
						playView.clearMessage();
						stopMusic();
					})

			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> musicLoading.isDone() && app().settings.getAsBoolean("PacManApp.skipIntro"))

				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> musicLoading.isDone())
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete())
				
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
				
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState()::onFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(playingState()::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(playingState()::onPacManGainsPower)
					
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
					.act(() -> ghostCommand.init())
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(() -> ghostCommand.init())
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !isGameOverMusicRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState, PacManGameEvent> {

		private long lastEatTime;

		@Override
		public void onEntry() {
			cast.ghostsOnStage().forEach(Ghost::show);
			cast.pacMan.setState(PacManState.ALIVE);
			playView.init();
			playView.enableAnimations();
			playView.startEnergizerBlinking();
		}

		@Override
		public void onTick() {

			ghostCommand.update();
			cheats.update();
			house.update();
			cast.actorsOnStage().forEach(Actor::update);
			cast.bonus().ifPresent(Bonus::update);
			if (System.currentTimeMillis() - lastEatTime > 250) {
				stopSoundPelletEaten();
			}
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			ghostCommand.suspend();
			cast.actorsOnStage().forEach(actor -> actor.process(event));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostCommand.resume();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			if (!cast.pacMan.is(PacManState.ALIVE)) {
				return;
			}
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			if (!collision.ghost.is(GhostState.FRIGHTENED)) {
				LOGGER.info(() -> String.format("Pac-Man killed by %s at %s", collision.ghost.name(), collision.ghost.tile()));
				house.enableAndResetGlobalDotCounter();
				stopSoundEffects();
				stopMusicPlaying();
				playView.stopEnergizerBlinking();
				cast.pacMan.process(new PacManKilledEvent(collision.ghost));
				enqueue(new PacManKilledEvent(collision.ghost));
			} else {
				LOGGER.info(() -> String.format("Ghost %s killed at %s", collision.ghost.name(), collision.ghost.tile()));
				int livesBefore = game.lives;
				game.scoreKilledGhost(collision.ghost.name());
				if (game.lives > livesBefore) {
					playSoundExtraLife();
				}
				playSoundGhostEaten();
				collision.ghost.process(new GhostKilledEvent(collision.ghost));
				enqueue(new GhostKilledEvent(collision.ghost));
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			cast.bonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found %s and wins %d points", bonus.symbol(), bonus.value()));
				int livesBefore = game.lives;
				game.score(bonus.value());
				playSoundBonusEaten();
				if (game.lives > livesBefore) {
					playSoundExtraLife();
				}
				bonus.process(event);
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent foodFound = (FoodFoundEvent) event;
			house.updateDotCounters();
			int points = game.eatFoodAt(foodFound.tile);
			int livesBefore = game.lives;
			game.score(points);
			if (!isSoundRunningPelletEaten()) {
				startSoundPelletEaten();
			}
			lastEatTime = System.currentTimeMillis();
			if (game.lives > livesBefore) {
				playSoundExtraLife();
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
			if (foodFound.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	// handle input

	private void onChangeClockSpeed() {
		int oldFreq = app().clock.getFrequency();
		int newFreq = oldFreq;
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			newFreq = Game.SPEED_1_FPS;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			newFreq = Game.SPEED_2_FPS;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			newFreq = Game.SPEED_3_FPS;
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_LEFT)) {
			newFreq = (oldFreq <= 10 ? Math.max(1, oldFreq - 1) : oldFreq - 5);
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_RIGHT)) {
			newFreq = (oldFreq < 10 ? oldFreq + 1 : oldFreq + 5);
		}
		if (newFreq != oldFreq) {
			app().clock.setFrequency(newFreq);
			LOGGER.info(String.format("Clock frequency changed to %d ticks/sec", newFreq));
		}
	}

	private void onChangePlayViewSettings() {
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

	private void onChangePacManOverflowBug() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_O)) {
			app().settings.set("PacMan.overflowBug", !app().settings.getAsBoolean("PacMan.overflowBug"));
			LOGGER.info("Overflow bug is " + (app().settings.getAsBoolean("PacMan.overflowBug") ? "on" : "off"));
		}
	}

	private void onChangeStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			FSM_LOGGER.setLevel(FSM_LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + FSM_LOGGER.getLevel());
		}
	}

	private void onChangeGhostFrightenedBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			boolean original = app().settings.getAsBoolean("Ghost.fleeRandomly");
			if (original) {
				app().settings.set("Ghost.fleeRandomly", false);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isFleeingToSafeCornerFrom(cast.pacMan)));
				LOGGER.info(() -> "Changed ghost escape behavior to escaping via safe route");
			} else {
				app().settings.set("Ghost.fleeRandomly", true);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isMovingRandomlyWithoutTurningBack()));
				LOGGER.info(() -> "Changed ghost escape behavior to original random movement");
			}
		}
	}

	// sounds

	private void loadMusic() {
		musicLoading = CompletableFuture.runAsync(() -> {
			theme.music_playing();
			theme.music_gameover();
		});
	}

	public void stopSoundEffects() {
		theme.snd_clips_all().forEach(Sound::stop);
	}

	public void stopMusic() {
		theme.music_gameover().stop();
		theme.music_playing().stop();
	}

	public void stopMusicPlaying() {
		theme.music_playing().stop();
	}

	public void loopMusicPlaying() {
		theme.music_playing().volume(.90f);
		theme.music_playing().loop();
	}

	public void playSoundReady() {
		theme.snd_ready().play();
	}

	public void startSoundPelletEaten() {
		theme.snd_eatPill().loop();
	}

	public void stopSoundPelletEaten() {
		theme.snd_eatPill().stop();
	}

	public boolean isSoundRunningPelletEaten() {
		return theme.snd_eatPill().isRunning();
	}

	public void playSoundGhostEaten() {
		theme.snd_eatGhost().play();
	}

	public void playSoundBonusEaten() {
		theme.snd_eatFruit().play();
	}

	public void playSoundPacManDied() {
		theme.snd_die().play();
	}

	public void playSoundExtraLife() {
		theme.snd_extraLife().play();
	}

	public void playSoundGameOver() {
		theme.music_gameover().play();
	}

	public boolean isGameOverMusicRunning() {
		return theme.music_gameover().isRunning();
	}
}