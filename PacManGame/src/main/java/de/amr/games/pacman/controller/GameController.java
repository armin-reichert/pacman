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
import static de.amr.games.pacman.model.Game.sec;
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
import de.amr.games.pacman.controller.ghosthouse.GhostHouseAccessControl;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.LoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.render.GhostRenderer;
import de.amr.games.pacman.view.render.PacManRenderer;
import de.amr.games.pacman.view.theme.ArcadeTheme;
import de.amr.games.pacman.view.theme.Theme;
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
	protected final Theme theme;
	protected final PacManSounds sound;

	protected GhostCommand ghostCommand;
	protected GhostHouseAccessControl ghostHouseAccessControl;
	protected BonusControl bonusControl;

	protected Game game;

	protected LivingView currentView;
	protected PlayView playView;

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

		theme = new ArcadeTheme();
		pacMan.setRenderer(new PacManRenderer(pacMan, theme));
		people.ghosts().forEach(ghost -> ghost.setRenderer(new GhostRenderer(ghost, theme)));

		people.creatures().forEach(creature -> {
			creature.addEventListener(this::process);
			world.include(creature);
		});

		sound = new PacManSounds(world, theme);

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
						sound.loadMusic();
						showView(new LoadingView(world, theme, settings.width, settings.height));
					})
					
				.state(INTRO)
					.onEntry(() -> {
						showView(new IntroView(theme, settings.width, settings.height));
					})
					.onExit(() -> {
						sound.stopAll();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						sound.gameReady();
						newGame();
						showView(playView);
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.showMessage("Ready!", Color.YELLOW);
							playView.turnEnergizerBlinkingOn();
							theme.music_playing().play();
						}
						creaturesOnStage().forEach(Creature::update);
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(mazeFlashingSeconds() + 6))
					.onEntry(() -> {
						pacMan.showFull();
						ghostHouseAccessControl.onLevelChange();
						sound.stopAllClips();
						playView.enableGhostAnimations(false);
						playView.turnEnergizerBlinkingOff();
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
					})
					.onTick((state, t, remaining) -> {
						float flashingSeconds = mazeFlashingSeconds();
	
						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (t == sec(2)) {
							ghostsOnStage().forEach(ghost -> ghost.visible = false);
							if (flashingSeconds > 0) {
								playView.turnMazeFlashingOn();
							}
						}
	
						// After flashing, show empty maze.
						if (t == sec(2 + flashingSeconds)) {
							playView.turnMazeFlashingOff();
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
						sound.stopAllClips();
					})
					.onTick((state, t, remaining) -> {
						int waitTime = sec(1f), 
								dyingStartTime = waitTime + sec(1.5f),
								dyingEndTime = dyingStartTime + sec(3f);
						if (t == waitTime) {
							bonusControl.deactivateBonus();
							ghostsOnStage().forEach(ghost -> ghost.visible = false);
							pacMan.getRenderer().selectSprite("full");
						}
						else if (t == dyingStartTime) {
							pacMan.getRenderer().selectSprite("dying");
							pacMan.getRenderer().enableSpriteAnimation(true);
							sound.pacManDied();
						}
						else if (t == dyingEndTime && game.lives > 0) {
							creaturesOnStage().forEach(Creature::init);
							playView.init();
						}
						else if (t > dyingEndTime) {
							ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						ghostsOnStage().forEach(ghost -> {
							ghost.init();
							ghost.placeAt(world.theHouse().bed(0).tile);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showMessage("Game Over!", Color.RED);
						sound.gameOver();
					})
					.onTick(() -> {
						ghostsOnStage().forEach(ghost -> {
							ghost.move();
							if (ghost.getState() == GhostState.FRIGHTENED) {
								ghost.showFrightened();
							} else {
								ghost.showColored();
							}
						});
					})
					.onExit(() -> {
						world.fillFood();
						ghostsOnStage().forEach(Ghost::init);
						playView.clearMessage();
						sound.stopAll();
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> sound.isMusicLoadingComplete()	&& settings.skipIntro)
	
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> sound.isMusicLoadingComplete())
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> currentView.isComplete())
					
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.act(playingState()::prepare)
				
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
					.act(playingState()::prepare)
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(playingState()::prepare)
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space"))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sound.isGameOverMusicRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	private void newGame() {
		game = new Game(settings.startLevel, world.totalFoodCount());

		ghostCommand = new GhostCommand(game, world.population().ghosts());
		ghostHouseAccessControl = new GhostHouseAccessControl(game, world, world.theHouse());
		bonusControl = new BonusControl(game, world);

		playView = new PlayView(world, theme, game, settings.width, settings.height);
		playView.optionalGhostCommand = ghostCommand;
		playView.optionalHouseAccessControl = ghostHouseAccessControl;

		world.population().ghosts().forEach(ghost -> {
			ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game));
			world.include(ghost);
		});

		world.include(pacMan);
		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));

		world.population().play(game);

		setDemoMode(settings.demoMode);
		app().f2Dialog().ifPresent(f2 -> f2.selectCustomTab(0));
	}

	private PlayingState playingState() {
		return state(PLAYING);
	}

	/**
	 * "PLAYING" state implementation.
	 */
	public class PlayingState extends State<PacManGameState> {

		void prepare() {
			playView.clearMessage();
			bonusControl.init();
			ghostCommand.init();
			creaturesOnStage().forEach(Creature::init);
			playView.init();
			playView.enableGhostAnimations(true);
			playView.turnEnergizerBlinkingOn();
			sound.resumePlayingMusic();
			pacMan.start();
		}

		@Override
		public void onTick() {
			ghostCommand.update();
			ghostHouseAccessControl.update();
			creaturesOnStage().forEach(Creature::update);
			bonusControl.update();
			sound.updatePlayingSounds();
		}

		@Override
		public void onExit() {
			sound.stopGhostSounds();
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
				game.scoreGhostKilled(ghost.name);
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
				ghostHouseAccessControl.onLifeLost();
				sound.stopAll();
				playView.turnEnergizerBlinkingOff();
				pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tile());
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			loginfo("PacMan found %s and wins %d points", game.level.bonusSymbol, game.level.bonusValue);
			int livesBefore = game.lives;
			game.score(game.level.bonusValue);
			sound.bonusEaten();
			if (game.lives > livesBefore) {
				sound.extraLife();
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
			sound.pelletEaten();
			if (game.lives > livesBeforeScoring) {
				sound.extraLife();
			}
			ghostHouseAccessControl.onPacManFoundFood();

			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}

			if (game.isBonusDue()) {
				bonusControl.activateBonus();
			}
			if (energizer && game.level.pacManPowerSeconds > 0) {
				sound.pacManGainsPower();
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

	public Theme theme() {
		return theme;
	}

	public Optional<Game> game() {
		return Optional.ofNullable(game);
	}

	public Optional<GhostCommand> ghostCommand() {
		return Optional.ofNullable(ghostCommand);
	}

	public Optional<GhostHouseAccessControl> ghostHouseAccess() {
		return Optional.of(ghostHouseAccessControl);
	}

	public Optional<BonusControl> bonusControl() {
		return Optional.of(bonusControl);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	public void setShowingActorRoutes(boolean selected) {
		playView.showingRoutes = selected;
	}

	public boolean isShowingActorRoutes() {
		return playView.showingRoutes;
	}

	public void setShowingGrid(boolean selected) {
		playView.showingGrid = selected;
	}

	public boolean isShowingGrid() {
		return playView.showingGrid;
	}

	public void setShowingStates(boolean selected) {
		playView.showingStates = selected;
	}

	public boolean isShowingStates() {
		return playView.showingStates;
	}

	public void setDemoMode(boolean on) {
		settings.pacManImmortable = on;
		if (on) {
			pacMan.behavior(new SearchingForFoodAndAvoidingGhosts(pacMan));
		} else {
			pacMan.behavior(pacMan.followingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
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