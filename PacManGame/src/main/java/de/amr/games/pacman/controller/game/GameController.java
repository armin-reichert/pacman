package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.game.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.game.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.game.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.game.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.game.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.game.PacManGameState.LOADING_MUSIC;
import static de.amr.games.pacman.controller.game.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.game.PacManGameState.PLAYING;
import static de.amr.games.pacman.model.game.Game.sec;
import static java.util.stream.IntStream.range;

import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
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
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Bonus;
import de.amr.games.pacman.model.world.components.BonusState;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.MusicLoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at
	 * level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	public static final float BASE_SPEED = 1.25f;

	public static float pacManSpeed(PacMan pacMan, Game game) {
		if (pacMan.is(PacManState.SLEEPING, PacManState.DEAD)) {
			return 0;
		}
		return pacMan.getPower() > 0 ? speed(game.level.pacManPowerSpeed) : speed(game.level.pacManSpeed);
	}

	public static float ghostSpeed(Ghost ghost, Game game) {
		switch (ghost.getState()) {
		case LOCKED:
			return speed(ghost.isInsideHouse() ? game.level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(game.level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(game.level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (ghost.world().isTunnel(ghost.tileLocation())) {
				return speed(game.level.ghostTunnelSpeed);
			}
			switch (ghost.getSanity()) {
			case ELROY1:
				return speed(game.level.elroy1Speed);
			case ELROY2:
				return speed(game.level.elroy2Speed);
			case INFECTABLE:
			case IMMUNE:
				return speed(game.level.ghostSpeed);
			default:
				throw new IllegalArgumentException("Illegal ghost sanity state: " + ghost.getSanity());
			}
		case FRIGHTENED:
			return speed(
					ghost.world().isTunnel(ghost.tileLocation()) ? game.level.ghostTunnelSpeed : game.level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ghost.getState()));
		}
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	protected final Theme[] themes = Themes.all().toArray(Theme[]::new);

	protected ArcadeWorld world;
	protected Folks folks;
	protected PacManSounds sound;

	protected GhostCommand ghostCommand;
	protected DoorMan doorMan;
	protected BonusControl bonusControl;

	protected Game game;

	protected int currentThemeIndex = 0;

	protected PacManGameView currentView;
	protected IntroView introView;
	protected MusicLoadingView musicLoadingView;
	protected PlayView playView;

	public GameController() {
		super(PacManGameState.class);
		app().onClose(() -> game().ifPresent(game -> game.hiscore.save()));
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		//@formatter:off
		beginStateMachine()
			
			.description("GameController")
			.initialState(LOADING_MUSIC)
			
			.states()
			
				.state(LOADING_MUSIC)
					.onEntry(() -> {
						sound.loadMusic();
						if (musicLoadingView != null) {
							musicLoadingView.exit();
						}
						musicLoadingView = new MusicLoadingView(theme(), settings.width, settings.height);
						showView(musicLoadingView);
					})
					.onExit(() -> {
						musicLoadingView.exit();
					})
					
				.state(INTRO)
					.onEntry(() -> {
						if (introView != null) {
							introView.exit();
						}
						introView = new IntroView(world, theme(), sound, settings.width, settings.height);
						showView(introView);
					})
					.onExit(() -> {
						sound.stopAll();
						introView.exit();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(7))
					.onEntry(() -> {
						sound.gameReady();
						newGame();
						world.setFrozen(true);
						showView(playView);
					})
					.onTick((state, t, remaining) -> {
						if (t == sec(5)) {
							playView.showGameReady();
							world.setFrozen(false);
							sound.music_playing().play();
						}
						folksInsideWorld().forEach(Creature::update);
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(6 + game.level.numFlashes * theme().$float("maze-flash-sec")))
					.onEntry(() -> {
						folks.pacMan.fallAsleep();
						doorMan.onLevelChange();
						sound.stopAllClips();
						playView.enableGhostAnimations(false);
						loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
					})
					.onTick((state, passed, remaining) -> {
						float flashingSeconds = game.level.numFlashes * theme().$float("maze-flash-sec");
						
						// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
						if (passed == sec(2)) {
							ghostsInsideWorld().forEach(ghost -> ghost.setVisible(false));
							if (flashingSeconds > 0) {
								world.setChanging(true);
							}
						}
	
						// After flashing, show empty maze.
						if (passed == sec(2 + flashingSeconds)) {
							world.setChanging(false);
						}
						
						// After two more seconds, change level and show crowded maze.
						if (passed == sec(4 + flashingSeconds)) {
							game.enterLevel(game.level.number + 1);
							world.fillFood();
							folksInsideWorld().forEach(Creature::init);
							playView.init();
						}
						
						// After two more seconds, enable ghost animations again
						if (passed == sec(6 + flashingSeconds)) {
							playView.enableGhostAnimations(true);
						}
						
						// Until end of state, let ghosts jump inside the house. 
						if (passed >= sec(6 + flashingSeconds)) {
							ghostsInsideWorld().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						folks.pacMan.setVisible(false);
					})
					.onTick(() -> {
						bonusControl.update();
						ghostsInsideWorld()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						folks.pacMan.setVisible(true);
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(7) : sec(5))
					.onEntry(() -> {
						game.lives -= settings.pacManImmortable ? 0 : 1;
						sound.stopAllClips();
						world.setFrozen(true);
					})
					.onTick((state, t, remaining) -> {
						int waitTime = sec(1f), 
								dyingStartTime = waitTime + sec(1.5f),
								dyingEndTime = dyingStartTime + sec(3f);
						if (t == waitTime) {
							bonusControl.setState(BonusState.INACTIVE);
							ghostsInsideWorld().forEach(ghost -> ghost.setVisible(false));
						}
						else if (t == dyingStartTime) {
							folks.pacMan.setCollapsing(true);
							sound.pacManDied();
						}
						else if (t == dyingEndTime && game.lives > 0) {
							folks.pacMan.setCollapsing(false);
							folksInsideWorld().forEach(Creature::init);
							playView.init();
						}
						else if (t > dyingEndTime) {
							ghostsInsideWorld().forEach(Ghost::update);
						}
					})
					.onExit(() -> {
						world.setFrozen(false);
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						ghostsInsideWorld().forEach(ghost -> {
							ghost.init();
							Bed bed = world.house(0).bed(0);
							ghost.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showGameOver();
						sound.gameOver();
					})
					.onTick(() -> {
						ghostsInsideWorld().forEach(ghost -> {
							ghost.move();
						});
					})
					.onExit(() -> {
						world.fillFood();
						ghostsInsideWorld().forEach(ghost -> {
							ghost.init();
						});
						playView.clearMessages();
						sound.stopAll();
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> sound.isMusicLoadingComplete()	&& settings.skipIntro)
					.annotation("music loaded, skipping intro")
					
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> sound.isMusicLoadingComplete())
					.annotation("music loaded")
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> currentView.isComplete())
					.annotation("intro complete")
					
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.act(playingState()::preparePlaying)
					.annotation("ready to play")
				
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
					.annotation("level change complete")
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					.annotation("resume playing")
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					.annotation("no lives left, game over")
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(playingState()::preparePlaying)
					.annotation(() -> String.format("%d lives remaining, if > 0, resume game", game.lives))
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space"))
					.annotation("new game requested")
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sound.isGameOverMusicRunning())
					.annotation("game over time complete")
							
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
			folks.pacMan.startRunning();
			playView.init();
			playView.enableGhostAnimations(true);
			sound.resumePlayingMusic();
		}

		@Override
		public void onTick(State<?> state, int consumed, int remaining) {
			ghostCommand.update();
			doorMan.update();
			bonusControl.update();
			folksInsideWorld().forEach(Creature::update);
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
				loginfo("%s got killed at %s", ghost.name, ghost.tileLocation());
				return;
			}

			if (!settings.ghostsHarmless) {
				doorMan.onLifeLost();
				sound.stopAll();
				folks.pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tileLocation());
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			BonusFoundEvent bonusFound = (BonusFoundEvent) event;
			Bonus bonus = bonusFound.bonus;
			loginfo("PacMan found bonus '%s' of value %d", bonus.symbol, bonus.value);
			int livesBefore = game.lives;
			game.score(bonus.value);
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
			world.clearFood(found.tile);
			if (energizer) {
				game.scoreEnergizerFound();
			} else {
				game.scoreSimplePelletFound();
			}
			if (game.lives > livesBeforeScoring) {
				sound.extraLife();
			}
			doorMan.onPacManFoundFood();
			sound.pelletEaten();

			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}

			if (game.isBonusDue()) {
				bonusControl.setState(BonusState.ACTIVE);
			}
			if (energizer && game.level.pacManPowerSeconds > 0) {
				sound.pacManGainsPower();
				ghostCommand.suspend();
				folks.pacMan.setPower(sec(game.level.pacManPowerSeconds));
				ghostsInsideWorld().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
			}
		}
	}

	@Override
	public void init() {
		loginfo("Initializing game controller");
		selectTheme(settings.theme);
		world = new ArcadeWorld();
		folks = new Folks(world, world.house(0));
		folks.all().forEach(world::include);
		folks.pacMan.addEventListener(this::process);
		folks.ghosts().forEach(ghost -> ghost.addEventListener(this::process));
		sound = new PacManSounds(world, folks);
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

	protected void newGame() {
		game = new Game(settings.startLevel, world.totalFoodCount());
		ghostCommand = new GhostCommand(game, folks);
		bonusControl = new BonusControl(game, world);
		doorMan = new DoorMan(world, world.house(0), game, folks);
		folks.ghosts().forEach(ghost -> ghost.getReadyToRumble(game));
		folks.pacMan.setSpeed(() -> GameController.pacManSpeed(folks.pacMan, game));
		folks.all().forEach(world::include);
		folks.all().forEach(Creature::init);
		playView = createPlayView();
	}

	protected PlayView createPlayView() {
		return new PlayView(world, theme(), folks, game, ghostCommand, doorMan);
	}

	protected PlayingState playingState() {
		return state(PLAYING);
	}

	public Folks folks() {
		return folks;
	}

	public Stream<Ghost> ghostsInsideWorld() {
		return folks.ghosts().filter(world::contains);
	}

	public Stream<Creature<?>> folksInsideWorld() {
		return folks.all().filter(world::contains);
	}

	public void selectTheme(String themeName) {
		currentThemeIndex = range(0, themes.length).filter(i -> themes[i].name().equalsIgnoreCase(themeName)).findFirst()
				.orElse(0);
		if (currentView != null) {
			currentView.setTheme(theme());
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
		return Optional.ofNullable(doorMan);
	}

	public Optional<BonusControl> bonusControl() {
		return Optional.ofNullable(bonusControl);
	}

	protected void showView(PacManGameView view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	public void changeClockFrequency(int ticksPerSecond) {
		if (app().clock().getTargetFramerate() != ticksPerSecond) {
			app().clock().setTargetFrameRate(ticksPerSecond);
			loginfo("Clock frequency changed to %d ticks/sec", ticksPerSecond);
		}
	}
}