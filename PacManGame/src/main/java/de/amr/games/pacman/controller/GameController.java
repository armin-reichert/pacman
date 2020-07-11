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
import static de.amr.games.pacman.model.game.Game.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.ArcadeWorldFolks;
import de.amr.games.pacman.controller.actor.BonusControl;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacman.controller.api.Creature;
import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.ghosthouse.DoorMan;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.MusicLoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	protected final Theme[] themes = { Themes.ARCADE_THEME, Themes.BLOCKS_THEME, Themes.LETTERS_THEME };

	protected World world;
	protected ArcadeWorldFolks folks;
	protected PacManSounds soundManager;

	protected GhostCommand ghostCommand;
	protected DoorMan doorMan;
	protected BonusControl bonusControl;

	protected Game game;

	protected int currentThemeIndex = 0;

	protected PacManGameView currentView;
	protected IntroView introView;
	protected PlayView playView;

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;
	private boolean showingScores = true;

	public GameController() {
		super(PacManGameState.class);
		app().onClose(() -> game().ifPresent(game -> game.hiscore.save()));
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(LOADING_MUSIC)
			
			.states()
			
				.state(LOADING_MUSIC)
					.onEntry(() -> {
						soundManager.loadMusic();
						showView(new MusicLoadingView(theme(), settings.width, settings.height));
					})
					
				.state(INTRO)
					.onEntry(() -> {
						introView = new IntroView(world, theme(), soundManager, settings.width, settings.height);
						showView(introView);
					})
					.onExit(() -> {
						soundManager.stopAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						soundManager.gameReady();
						newGame();
						world.setFrozen(true);
						showView(playView);
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.showGameReady();
							world.setFrozen(false);
							soundManager.music_playing().play();
						}
						folksInsideWorld().forEach(Creature::update);
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						folks.pacMan().tf.setVelocity(0, 0);
						doorMan.onLevelChange();
						soundManager.stopAllClips();
						playView.enableGhostAnimations(false);
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
					})
					.onTick((state, t, remaining) -> {
						float flashingSeconds = mazeFlashingSeconds();
	
						// let Pac-Man sleep at the current position
						folks.pacMan().fallAsleep();
						
						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							folks.ghostsInsideWorld().forEach(ghost -> ghost.visible = false);
							if (flashingSeconds > 0) {
								world.setChangingLevel(true);
							}
						}
	
						// After flashing, show empty maze.
						if (t == sec(2 + flashingSeconds)) {
							world.setChangingLevel(false);
						}
						
						// After two more seconds, change level and show crowded maze.
						if (t == sec(4 + flashingSeconds)) {
							game.enterLevel(game.level.number + 1);
							world.fillFood();
							folksInsideWorld().forEach(Creature::init);
							playView.init();
						}
						
						// After two more seconds, enable ghost animations again
						if (t == sec(6 + flashingSeconds)) {
							playView.enableGhostAnimations(true);
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (t >= sec(6 + flashingSeconds)) {
							folks.ghostsInsideWorld().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						folks.pacMan().visible = false;
					})
					.onTick(() -> {
						bonusControl.update();
						folks.ghostsInsideWorld()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						folks.pacMan().visible = true;
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(7) : sec(5))
					.onEntry(() -> {
						game.lives -= settings.pacManImmortable ? 0 : 1;
						soundManager.stopAllClips();
						world.setFrozen(true);
					})
					.onTick((state, t, remaining) -> {
						int waitTime = sec(1f), 
								dyingStartTime = waitTime + sec(1.5f),
								dyingEndTime = dyingStartTime + sec(3f);
						if (t == waitTime) {
							bonusControl.deactivateBonus();
							folks.ghostsInsideWorld().forEach(ghost -> ghost.visible = false);
						}
						else if (t == dyingStartTime) {
							folks.pacMan().setCollapsing(true);
							soundManager.pacManDied();
						}
						else if (t == dyingEndTime && game.lives > 0) {
							folks.pacMan().setCollapsing(false);
							folksInsideWorld().forEach(Creature::init);
							playView.init();
						}
						else if (t > dyingEndTime) {
							folks.ghostsInsideWorld().forEach(Ghost::update);
						}
					})
					.onExit(() -> {
						world.setFrozen(false);
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						folks.ghostsInsideWorld().forEach(ghost -> {
							ghost.init();
							ghost.placeAt(world.theHouse().bed(0).tile);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showGameOver();
						soundManager.gameOver();
					})
					.onTick(() -> {
						folks.ghostsInsideWorld().forEach(ghost -> {
							ghost.move();
						});
					})
					.onExit(() -> {
						world.fillFood();
						folks.ghostsInsideWorld().forEach(ghost -> {
							ghost.init();
						});
						playView.clearMessages();
						soundManager.stopAll();
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> soundManager.isMusicLoadingComplete()	&& settings.skipIntro)
	
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> soundManager.isMusicLoadingComplete())
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> currentView.isComplete())
					
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.act(playingState()::preparePlaying)
				
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
					.act(playingState()::preparePlaying)
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(playingState()::preparePlaying)
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space"))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !soundManager.isGameOverMusicRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState> {

		private void preparePlaying() {
			world.setFrozen(false);
			bonusControl.init();
			ghostCommand.init();
			folksInsideWorld().forEach(Creature::init);
			folks.pacMan().startRunning();
			playView.init();
			playView.enableGhostAnimations(true);
			soundManager.resumePlayingMusic();
		}

		@Override
		public void onTick() {
			ghostCommand.update();
			doorMan.update();
			bonusControl.update();
			folksInsideWorld().forEach(Creature::update);
			soundManager.updatePlayingSounds();
		}

		@Override
		public void onExit() {
			soundManager.stopGhostSounds();
		}

		private void onPacManLostPower(PacManGameEvent event) {
			soundManager.pacManLostPower();
			ghostCommand.resume();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			if (ghost.is(FRIGHTENED)) {
				int livesBefore = game.lives;
				game.scoreGhostKilled(ghost.name());
				if (game.lives > livesBefore) {
					soundManager.extraLife();
				}
				soundManager.ghostEaten();
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("%s got killed at %s", ghost.name(), ghost.location());
				return;
			}

			if (!settings.ghostsHarmless) {
				doorMan.onLifeLost();
				soundManager.stopAll();
				folks.pacMan().process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name(), ghost.location());
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			loginfo("PacMan found %s and wins %d points", game.level.bonusSymbol, game.level.bonusValue);
			int livesBefore = game.lives;
			game.score(game.level.bonusValue);
			soundManager.bonusEaten();
			if (game.lives > livesBefore) {
				soundManager.extraLife();
			}
			bonusControl.process(event);
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			int livesBeforeScoring = game.lives;
			boolean energizer = world.containsEnergizer(found.tile);
			world.clearFood(found.tile);
			if (energizer) {
				game.scoreEnergizerFound();
			} else {
				game.scoreSimplePelletFound();
			}
			if (game.lives > livesBeforeScoring) {
				soundManager.extraLife();
			}
			doorMan.onPacManFoundFood();
			soundManager.pelletEaten();

			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}

			if (game.isBonusDue()) {
				bonusControl.activateBonus();
			}
			if (energizer && game.level.pacManPowerSeconds > 0) {
				soundManager.pacManGainsPower();
				ghostCommand.suspend();
				folks.pacMan().setPower(sec(game.level.pacManPowerSeconds));
				folks.ghostsInsideWorld().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
			}
		}
	}

	@Override
	public void init() {
		loginfo("Initializing game controller");
		selectTheme(settings.theme);
		world = new ArcadeWorld();
		folks = new ArcadeWorldFolks(world);
		folks.all().forEach(world::bringIn);
		folks.pacMan().addEventListener(this::process);
		folks.ghosts().forEach(ghost -> ghost.addEventListener(this::process));
		soundManager = new PacManSounds(world, folks);
		super.init();
	}

	@Override
	public void update() {
		handleInput();
		super.update();
		currentView.update();
	}

	private void handleInput() {
		if (Keyboard.keyPressedOnce("1") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			changeClockFrequency(60);
		} else if (Keyboard.keyPressedOnce("2") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			changeClockFrequency(70);
		} else if (Keyboard.keyPressedOnce("3") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			changeClockFrequency(80);
		} else if (Keyboard.keyPressedOnce("z")) {
			currentThemeIndex = (currentThemeIndex + 1) % themes.length;
			currentView.setTheme(theme());
		}
	}

	private void newGame() {
		game = new Game(settings.startLevel, world.totalFoodCount());
		ghostCommand = new GhostCommand(game, folks);
		doorMan = new DoorMan(world.theHouse(), game, folks);
		bonusControl = new BonusControl(game, world);
		folks.ghosts().forEach(ghost -> ghost.getReadyToRumble(game));
		folks.ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> SpeedLimits.speedLimit(ghost, game)));
		folks.pacMan().setSpeedLimit(() -> SpeedLimits.pacManSpeedLimit(folks.pacMan(), game));
		folks.all().forEach(world::bringIn);
		folks.all().forEach(Creature::init);
		playView = new PlayView(world, folks, game, ghostCommand, doorMan);
		playView.setTheme(theme());
		app().f2Dialog().ifPresent(f2 -> f2.selectCustomTab(0));
	}

	private PlayingState playingState() {
		return state(PLAYING);
	}

	public Stream<MobileCreature> folksInsideWorld() {
		return folks.all().filter(world::contains);
	}

	public void selectTheme(String themeName) {
		switch (themeName.toUpperCase()) {
		case "ARCADE":
			currentThemeIndex = 0;
			break;
		case "BLOCKS":
			currentThemeIndex = 1;
			break;
		case "LETTERS":
			currentThemeIndex = 2;
			break;
		default:
			currentThemeIndex = 0;
			break;
		}
		if (playView != null) {
			playView.setTheme(theme());
		}
	}

	public Theme theme() {
		return themes[currentThemeIndex];
	}

	public World world() {
		return world;
	}

	public Optional<Game> game() {
		return Optional.ofNullable(game);
	}

	public Optional<GhostCommand> ghostCommand() {
		return Optional.ofNullable(ghostCommand);
	}

	public Optional<DoorMan> doorMan() {
		return Optional.of(doorMan);
	}

	public Optional<BonusControl> bonusControl() {
		return Optional.of(bonusControl);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	public void setShowingRoutes(boolean selected) {
		showingRoutes = selected;
		if (selected) {
			playView.turnRoutesOn();
		} else {
			playView.turnRoutesOff();
		}
	}

	public boolean isShowingRoutes() {
		return showingRoutes;
	}

	public void setShowingGrid(boolean selected) {
		showingGrid = selected;
		if (selected) {
			playView.turnGridOn();
		} else {
			playView.turnGridOff();
		}
	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public boolean isShowingStates() {
		return showingStates;
	}

	public void setShowingStates(boolean selected) {
		showingStates = selected;
		if (selected) {
			playView.turnStatesOn();
		} else {
			playView.turnStatesOff();
		}
	}

	public boolean isShowingScores() {
		return showingScores;
	}

	public void setShowingScores(boolean selected) {
		showingScores = selected;
		if (selected) {
			playView.turnScoresOn();
		} else {
			playView.turnScoresOff();
		}
	}

	public void setDemoMode(boolean demoMode) {
		if (demoMode) {
			settings.pacManImmortable = true;
			playView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
			folks.pacMan().behavior(new SearchingForFoodAndAvoidingGhosts(folks.pacMan(), folks));
		} else {
			settings.pacManImmortable = false;
			playView.clearMessage(1);
			folks.pacMan().behavior(folks.pacMan().followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
	}

	protected void showView(PacManGameView view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	protected void changeClockFrequency(int newValue) {
		if (app().clock().getTargetFramerate() != newValue) {
			app().clock().setTargetFrameRate(newValue);
			loginfo("Clock frequency changed to %d ticks/sec", newValue);
		}
	}

	private float mazeFlashingSeconds() {
		return game.level.numFlashes * 0.4f;
	}
}