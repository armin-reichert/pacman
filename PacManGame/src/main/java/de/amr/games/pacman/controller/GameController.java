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
import static de.amr.games.pacman.controller.SpeedLimits.ghostSpeedLimit;
import static de.amr.games.pacman.controller.SpeedLimits.pacManSpeedLimit;
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
import de.amr.games.pacman.controller.actor.BonusControl;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.DefaultPopulation;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
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
import de.amr.games.pacman.controller.ghosthouse.GhostHouseDoorMan;
import de.amr.games.pacman.controller.sound.PacManSoundManager;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.LoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	protected final World world;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final PacManSoundManager soundManager;

	protected GhostCommand ghostCommand;
	protected GhostHouseDoorMan doorMan;
	protected BonusControl bonusControl;

	protected Game game;

	protected Theme[] themes = { Themes.ARCADE_THEME, Themes.BLOCKS_THEME, Themes.ASCII_THEME };
	protected int currentThemeIndex = 0;

	protected LivingView currentView;
	protected PlayView playView;

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;
	private boolean showingScores = true;

	public GameController() {
		super(PacManGameState.class);
		buildStateMachine();

		loginfo("Initializing game controller");

		Population people = new DefaultPopulation();
		pacMan = people.pacMan();
		blinky = people.blinky();
		pinky = people.pinky();
		inky = people.inky();
		clyde = people.clyde();

		world = Universe.arcadeWorld();
		people.populate(world);

		people.creatures().forEach(creature -> {
			creature.addEventListener(this::process);
			world.include(creature);
		});

		soundManager = new PacManSoundManager(world);

		app().onClose(() -> game().ifPresent(game -> game.hiscore.save()));
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
		}
		if (Keyboard.keyPressedOnce("z")) {
			currentThemeIndex = (currentThemeIndex + 1) % themes.length;
			playView.setTheme(themes[currentThemeIndex]);
		}
	}

	private void buildStateMachine() {
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
						showView(new LoadingView(world, settings.width, settings.height));
					})
					
				.state(INTRO)
					.onEntry(() -> {
						showView(new IntroView(soundManager, settings.width, settings.height));
					})
					.onExit(() -> {
						soundManager.stopAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						soundManager.gameReady();
						newGame();
						showView(playView);
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.showGameReady();
							world.setFrozen(false);
							soundManager.music_playing().play();
						}
						creaturesOnStage().forEach(Creature::update);
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						pacMan.tf.setVelocity(0, 0);
						doorMan.onLevelChange();
						soundManager.stopAllClips();
						playView.enableGhostAnimations(false);
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
					})
					.onTick((state, t, remaining) -> {
						float flashingSeconds = mazeFlashingSeconds();
	
						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							ghostsOnStage().forEach(ghost -> ghost.visible = false);
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
							creaturesOnStage().forEach(Creature::init);
							playView.init();
						}
						
						// After two more seconds, enable ghost animations again
						if (t == sec(6 + flashingSeconds)) {
							playView.enableGhostAnimations(true);
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (t >= sec(6 + flashingSeconds)) {
							ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						pacMan.visible = false;
					})
					.onTick(() -> {
						bonusControl.update();
						ghostsOnStage()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						pacMan.visible = true;
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
							ghostsOnStage().forEach(ghost -> ghost.visible = false);
						}
						else if (t == dyingStartTime) {
							pacMan.collapsing = true;
							soundManager.pacManDied();
						}
						else if (t == dyingEndTime && game.lives > 0) {
							pacMan.collapsing = false;
							creaturesOnStage().forEach(Creature::init);
							playView.init();
						}
						else if (t > dyingEndTime) {
							ghostsOnStage().forEach(Ghost::update);
						}
					})
					.onExit(() -> {
						world.setFrozen(false);
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						ghostsOnStage().forEach(ghost -> {
							ghost.init();
							ghost.placeAt(world.theHouse().bed(0).tile);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showGameOver();
						soundManager.gameOver();
					})
					.onTick(() -> {
						ghostsOnStage().forEach(ghost -> {
							ghost.move();
						});
					})
					.onExit(() -> {
						world.fillFood();
						ghostsOnStage().forEach(Ghost::init);
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

	private void newGame() {
		game = new Game(settings.startLevel, world.totalFoodCount());

		ghostCommand = new GhostCommand(game, world);
		doorMan = new GhostHouseDoorMan(game, world);
		bonusControl = new BonusControl(game, world);

		world.setFrozen(true);
		world.population().creatures().forEach(world::include);
		world.population().creatures().forEach(Creature::init);
		world.population().ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game)));
		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		world.population().play(game);

		playView = new PlayView(world, game, ghostCommand, doorMan);
		playView.setTheme(themes[currentThemeIndex]);

		app().f2Dialog().ifPresent(f2 -> f2.selectCustomTab(0));
	}

	private PlayingState playingState() {
		return state(PLAYING);
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState> {

		private void preparePlaying() {
			world.setFrozen(false);
			bonusControl.init();
			ghostCommand.init();
			creaturesOnStage().forEach(Creature::init);
			pacMan.start();
			playView.init();
			playView.enableGhostAnimations(true);
			soundManager.resumePlayingMusic();
		}

		@Override
		public void onTick() {
			ghostCommand.update();
			doorMan.update();
			creaturesOnStage().forEach(Creature::update);
			bonusControl.update();
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
				game.scoreGhostKilled(ghost.name);
				if (game.lives > livesBefore) {
					soundManager.extraLife();
				}
				soundManager.ghostEaten();
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("%s got killed at %s", ghost.name, ghost.tile());
				return;
			}

			if (!settings.ghostsHarmless) {
				doorMan.onLifeLost();
				soundManager.stopAll();
				pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tile());
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
			world.removeFood(found.tile);
			if (energizer) {
				game.scoreEnergizerFound();
			} else {
				game.scoreSimplePelletFound();
			}
			soundManager.pelletEaten();
			if (game.lives > livesBeforeScoring) {
				soundManager.extraLife();
			}
			doorMan.onPacManFoundFood();

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
				pacMan.power = sec(game.level.pacManPowerSeconds);
				ghostsOnStage().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
			}
		}
	}

	protected Stream<Creature<?>> creaturesOnStage() {
		return world.population().creatures().filter(world::included);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return world.population().ghosts().filter(world::included);
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

	public Optional<GhostHouseDoorMan> ghostHouseAccess() {
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
			playView.turnShowingRoutesOn();
		} else {
			playView.turnShowingRoutesOff();
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

	public boolean isShowingStates() {
		return showingStates;
	}

	public void setDemoMode(boolean on) {
		settings.pacManImmortable = on;
		if (on) {
			pacMan.behavior(new SearchingForFoodAndAvoidingGhosts(pacMan));
		} else {
			pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
		playView.showMessage(1, on ? "Demo Mode" : "", Color.LIGHT_GRAY);
	}

	protected void showView(LivingView view) {
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