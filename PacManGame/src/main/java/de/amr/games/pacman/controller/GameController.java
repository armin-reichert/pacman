package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.LOADING_MUSIC;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.Game.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.Random;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.actor.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.core.BaseView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.LoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.play.SimplePlayView.MazeMode;
import de.amr.games.pacman.view.settings.GameStateView;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	public Game game;

	protected Theme theme;
	protected PacManSounds sound;

	protected LoadingView loadingView;
	protected IntroView introView;
	protected PlayView playView;
	protected BaseView currentView;

	protected GameStateView gameStateView;

	public GhostCommand ghostCommand;
	protected GhostHouse ghostHouse;

	public GameController(Theme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		loadingView = new LoadingView(theme);
		introView = new IntroView(theme);
		sound = new PacManSounds(theme);
		buildStateMachine();
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent && !((FoodFoundEvent) e).energizer);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	private void showView(BaseView view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	private void createPlayEnvironment() {
		game = new Game(settings.startLevel);
		playView = new PlayView(game, theme);
		ghostCommand = new GhostCommand(game);
		ghostHouse = new GhostHouse(game);
		playView.fnGhostCommandState = ghostCommand::state;
		playView.house = ghostHouse;
		game.creatures().forEach(actor -> {
			game.putOnStage(actor);
			actor.addEventListener(this::process);
		});
		setDemoMode(settings.demoMode);

		// add custom tab in settings dialog
		if (gameStateView == null) {
			gameStateView = new GameStateView();
			app().addCustomSettingsTab("Game State", gameStateView);
		}
		gameStateView.attach(this);
	}

	public void saveScore() {
		if (game == null) {
			return;
		}
		game.gameScore.save();
	}

	public void setDemoMode(boolean on) {
		settings.pacManImmortable = on;
		if (on) {
			game.pacMan.behavior(new SearchingForFoodAndAvoidingGhosts(game));
		} else {
			game.pacMan.behavior(game.pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
	}

	protected void changeClockFrequency(int newValue) {
		if (app().clock().getTargetFramerate() != newValue) {
			app().clock().setTargetFrameRate(newValue);
			loginfo("Clock frequency changed to %d ticks/sec", newValue);
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce("1") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			changeClockFrequency(60);
		} else if (Keyboard.keyPressedOnce("2") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			changeClockFrequency(70);
		} else if (Keyboard.keyPressedOnce("3") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			changeClockFrequency(80);
		}
		if (eventQ.size() > 1) {
			PacManStateMachineLogging.loginfo("%s: Event queue has more than one entry: %s", getDescription(), eventQ);
		}
		super.update();
		currentView.update();
		if (gameStateView != null) {
			gameStateView.model.update();
		}
	}

	private float mazeFlashingSeconds() {
		return game.level.numFlashes * 0.4f;
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
						sound.stopAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						createPlayEnvironment();
						showView(playView);
						sound.gameReady();
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.showMessage("Ready!", Color.YELLOW);
							playView.mazeView.energizersBlinking.setEnabled(true);
							theme.music_playing().play();
						}
						game.creaturesOnStage().forEach(Creature::update);
					})
					.onExit(() -> {
						playView.clearMessage();
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						game.pacMan.showFull();
						ghostHouse.onLevelChange();
						sound.stopAllClips();
						playView.enableGhostAnimations(false);
						playView.mazeView.energizersBlinking.setEnabled(false);
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
					})
					.onTick((state, t, remaining) -> {
						float flashingSeconds = mazeFlashingSeconds();

						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							game.ghostsOnStage().forEach(ghost -> ghost.visible = false);
							if (flashingSeconds > 0) {
								playView.mazeView.setState(MazeMode.FLASHING);
							}
						}

						// After flashing, show empty maze.
						if (t == sec(2 + flashingSeconds)) {
							playView.mazeView.setState(MazeMode.EMPTY);
						}
						
						// After two more seconds, change level and show crowded maze.
						if (t == sec(4 + flashingSeconds)) {
							game.enterLevel(game.level.number + 1);
							game.creaturesOnStage().forEach(Creature::init);
							playView.init();
						}
						
						// After two more seconds, enable ghost animations again
						if (t == sec(6 + flashingSeconds)) {
							playView.enableGhostAnimations(true);
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (t >= sec(6 + flashingSeconds)) {
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
					.timeoutAfter(() -> game.lives > 1 ? sec(7) : sec(5))
					.onEntry(() -> {
						game.lives -= settings.pacManImmortable ? 0 : 1;
						sound.stopAllClips();
					})
					.onTick((state, t, remaining) -> {
						int waitTime = sec(1f), 
								dyingStartTime = waitTime + sec(1.5f),
								dyingEndTime = dyingStartTime + sec(3f);
						if (t == waitTime) {
							game.bonus.deactivate();
							game.ghostsOnStage().forEach(ghost -> ghost.visible = false);
							game.pacMan.showFull().enableAnimation(false);
						}
						else if (t == dyingStartTime) {
							game.pacMan.showDying().enableAnimation(true);
							sound.pacManDied();
						}
						else if (t == dyingEndTime && game.lives > 0) {
							game.creaturesOnStage().forEach(Creature::init);
							playView.init();
						}
						else if (t > dyingEndTime) {
							game.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						game.ghostsOnStage().forEach(ghost -> {
							ghost.init();
							ghost.placeAt(game.maze.ghostSeats[0].tile);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showMessage("Game Over!", Color.RED);
						sound.gameOver();
					})
					.onTick(() -> {
						game.ghostsOnStage().forEach(ghost -> {
							ghost.move();
							if (ghost.getState() == GhostState.FRIGHTENED) {
								ghost.showFrightened();
							} else {
								ghost.showColored();
							}
						});
					})
					.onExit(() -> {
						game.ghostsOnStage().forEach(ghost -> {
							ghost.init();
						});
						playView.clearMessage();
						sound.stopAll();
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
			game.creaturesOnStage().forEach(Creature::update);
			game.bonus.update();
			sound.updatePlayingSounds(game);
		}

		@Override
		public void onExit() {
			sound.stopGhostSounds();
		}

		private void reset() {
			sound.resumePlayingMusic();
			ghostCommand.init();
			game.ghostsOnStage().forEach(ghost -> ghost.init());
			game.pacMan.init();
			game.pacMan.setState(PacManState.EATING);
			playView.init();
			playView.enableGhostAnimations(true);
			playView.mazeView.energizersBlinking.setEnabled(true);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			sound.pacManLostPower();
			ghostCommand.resume();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			if (ghost.is(FRIGHTENED)) {
				int livesBefore = game.lives;
				game.scoreGhostKilled(ghost);
				if (game.lives > livesBefore) {
					sound.extraLife();
				}
				sound.ghostEaten();
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("%s got killed at %s", ghost.name, ghost.tile());
				return;
			}

			if (!settings.ghostsHarmless) {
				ghostHouse.onLifeLost();
				sound.stopAll();
				playView.mazeView.energizersBlinking.setEnabled(false);
				game.pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tile());
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			loginfo("PacMan found %s and wins %d points", game.bonus.symbol, game.bonus.value);
			int livesBefore = game.lives;
			game.score(game.bonus.value);
			sound.bonusEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
			}
			game.bonus.process(event);
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			ghostHouse.onPacManFoundFood();
			int points = game.eatFood(found.tile, found.energizer);
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
			if (game.isBonusDue()) {
				game.bonus.activate(theme);
				loginfo("Bonus %s activated, time: %.2f sec", game.bonus, game.bonus.state().getDuration() / 60f);
			}
			if (found.energizer && game.level.pacManPowerSeconds > 0) {
				sound.pacManGainsPower();
				ghostCommand.suspend();
				game.pacMan.power = sec(game.level.pacManPowerSeconds);
				game.ghostsOnStage().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
			}
		}
	}
}