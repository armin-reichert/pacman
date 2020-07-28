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
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;
import static de.amr.games.pacman.model.game.Game.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.assets.SoundClip;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.Folks;
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
import de.amr.games.pacman.controller.steering.pacman.SearchingForFoodAndAvoidingGhosts;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.game.GameLevel;
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
import de.amr.games.pacman.view.theme.api.PacManSounds;
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

	/**
	 * Pac-Man move speed at given game level.
	 * 
	 * @param pacMan Pac-Man
	 * @param level  game level
	 * @return speed in pixels per tick
	 */
	public static float pacManSpeed(PacMan pacMan, GameLevel level) {
		Objects.requireNonNull(pacMan);
		Objects.requireNonNull(level);
		if (pacMan.getState() == null) {
			throw new IllegalStateException("Pac-Man is not initialized.");
		}
		switch (pacMan.getState()) {
		case TIRED:
		case SLEEPING:
		case DEAD:
		case COLLAPSING:
			return 0;
		case POWERFUL:
			return speed(pacMan.mustDigest() ? level.pacManPowerDotsSpeed : level.pacManPowerSpeed);
		case AWAKE:
			return speed(pacMan.mustDigest() ? level.pacManDotsSpeed : level.pacManSpeed);
		default:
			throw new IllegalStateException("Illegal Pac-Man state: " + pacMan.getState());
		}
	}

	/**
	 * Ghost move speed at given game level.
	 * 
	 * @param ghost ghost
	 * @param level game level
	 * @return speed in pixels per tick
	 */
	public static float ghostSpeed(Ghost ghost, GameLevel level) {
		Objects.requireNonNull(ghost);
		Objects.requireNonNull(level);
		if (ghost.getState() == null) {
			throw new IllegalStateException(String.format("Ghost %s is not initialized.", ghost.name));
		}
		switch (ghost.getState()) {
		case LOCKED:
			return speed(ghost.isInsideHouse() ? level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (ghost.world().isTunnel(ghost.tileLocation())) {
				return speed(level.ghostTunnelSpeed);
			}
			switch (ghost.getSanity()) {
			case ELROY1:
				return speed(level.elroy1Speed);
			case ELROY2:
				return speed(level.elroy2Speed);
			case INFECTABLE:
			case IMMUNE:
				return speed(level.ghostSpeed);
			default:
				throw new IllegalArgumentException("Illegal ghost sanity state: " + ghost.getSanity());
			}
		case FRIGHTENED:
			return speed(ghost.world().isTunnel(ghost.tileLocation()) ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * level.ghostSpeed);
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

	protected final Theme[] themes;

	protected ArcadeWorld world;
	protected Folks folks;
	protected PacManSounds sounds;

	protected GhostCommand ghostCommand;
	protected DoorMan doorMan;
	protected BonusControl bonusControl;

	protected Game game;

	protected int currentThemeIndex = 0;

	protected PacManGameView currentView;
	protected IntroView introView;
	protected MusicLoadingView musicLoadingView;
	protected PlayView playView;

	/**
	 * Creates a new game controller.
	 * 
	 * @param themes supported themes
	 */
	public GameController(Stream<Theme> themes) {
		super(PacManGameState.class);

		this.themes = themes.toArray(Theme[]::new);

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
						sounds.loadMusic();
						if (musicLoadingView != null) {
							musicLoadingView.exit();
						}
						musicLoadingView = new MusicLoadingView(theme(), settings.width, settings.height);
						musicLoadingView.init();
						currentView = musicLoadingView;
					})
					.onExit(() -> {
						musicLoadingView.exit();
					})
					
				.state(INTRO)
					.onEntry(() -> {
						if (introView != null) {
							introView.exit();
						}
						introView = new IntroView(world, folks, theme(), settings.width, settings.height);
						introView.init();
						currentView = introView;
					})
					.onExit(() -> {
						sounds.stopAll();
						introView.exit();
					})
				
				.state(GETTING_READY).customState(new GettingReadyState())
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL).customState(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						folks.pacMan.setVisible(false);
					})
					.onTick(() -> {
						bonusControl.update();
						folks.ghostsInWorld()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						folks.pacMan.setVisible(true);
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(sec(4))
					.onEntry(() -> {
						if (!settings.pacManImmortable) {
							game.lives -= 1;
						}
						world.setFrozen(true);
						sounds.clips().forEach(SoundClip::stop);
					})
					.onTick((state, passed, remaining) -> {
						if (passed == sec(1)) {
							bonusControl.setState(BonusState.INACTIVE);
							folks.ghostsInWorld().forEach(ghost -> ghost.setVisible(false));
						}
						else if (remaining == sec(2.5f)) {
							sounds.stopMusic(sounds.musicGameRunning());
							sounds.clipPacManDies().play();
						}
						folks.pacMan.update();
					})
					.onExit(() -> {
						world.setFrozen(false);
						if (game.lives > 0) {
							folks.allInWorld().forEach(Creature::init);
							playView.init();
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						folks.ghostsInWorld().forEach(ghost -> {
							ghost.init();
							Bed bed = world.house(0).bed(0);
							ghost.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showMessage(2, "Game Over!", Color.RED);
						sounds.stopMusic(sounds.musicGameRunning());
						sounds.playMusic(sounds.musicGameOver());
					})
					.onTick(() -> {
						folks.ghostsInWorld().forEach(Ghost::move);
					})
					.onExit(() -> {
						playView.clearMessage(2);
						sounds.stopAll();
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> sounds.isMusicLoaded()	&& settings.skipIntro)
					.annotation("Music loaded, skipping intro")
					
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> sounds.isMusicLoaded())
					.annotation("Music loaded")

				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete())
					.annotation("Intro complete")
					
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.annotation("Ready to play")
				
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
					.act(playingState()::resumePlaying)
					.annotation("Level change complete")
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					.annotation("Resume playing")
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					.annotation("No lives left, game over")
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(playingState()::resumePlaying)
					.annotation(() -> String.format("Lives remaining = %d, resume game", game.lives))
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space") || Keyboard.keyPressedOnce("enter"))
					.annotation("New game requested by user")
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sounds.isMusicRunning(sounds.musicGameOver()))
					.annotation("Game over music finished")
							
		.endStateMachine();
		//@formatter:on
	}

	private class GettingReadyState extends State<PacManGameState> {

		public GettingReadyState() {
			setTimer(sec(6));
		}

		@Override
		public void onEntry() {
			newGame();
			world.setFrozen(true);
			currentView = playView = createPlayView();
			playView.init();
			playView.showMessage(2, "Ready!", Color.YELLOW);
			sounds.playMusic(sounds.musicGameReady());
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			if (remaining == sec(1)) {
				world.setFrozen(false);
			}
			folks.allInWorld().forEach(Creature::update);
		}

		@Override
		public void onExit() {
			playView.clearMessage(2);
		}
	}

	private class PlayingState extends State<PacManGameState> {

		final long INITIAL_WAIT_TIME = sec(2);

		private long lastMealTime;
		private boolean extraLife;
		private boolean ghostEaten;

		public PlayingState() {
		}

		@Override
		public void onEntry() {
			setDemoMode(settings.demoMode);
			sounds.musicGameRunning().ifPresent(music -> {
				music.volume(0.5f);
				music.loop();
			});
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			extraLife = false;
			ghostEaten = false;
			if (passed == INITIAL_WAIT_TIME) {
				folks.pacMan.wakeUp();
			} else if (passed > INITIAL_WAIT_TIME) {
				ghostCommand.update();
				doorMan.update();
				bonusControl.update();
			}
			folks.allInWorld().forEach(Creature::update);
			updateSound();
		}

		@Override
		public void onExit() {
			sounds.clips().forEach(SoundClip::stop);
		}

		private void resumePlaying() {
			world.setFrozen(false);
			bonusControl.init();
			ghostCommand.init();
			folks.allInWorld().forEach(Creature::init);
			playView.enableGhostAnimations(true);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			sounds.clipWaza().stop();
			ghostCommand.resumeAttacking();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			if (ghost.is(FRIGHTENED)) {
				int livesBefore = game.lives;
				game.scoreGhostKilled(ghost.name);
				if (game.lives > livesBefore) {
					extraLife = true;
				}
				ghostEaten = true;
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("%s got killed at %s", ghost.name, ghost.tileLocation());
				return;
			}

			if (!settings.ghostsHarmless) {
				doorMan.onLifeLost();
				sounds.stopAll();
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
			sounds.clipEatFruit().play();
			if (game.lives > livesBefore) {
				sounds.clipExtraLife().play();
			}
			bonusControl.process(event);
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			lastMealTime = System.currentTimeMillis();
			doorMan.onPacManFoundFood();
			boolean energizer = world.containsEnergizer(found.tile);
			world.clearFood(found.tile);
			int livesBeforeScoring = game.lives;
			if (energizer) {
				game.scoreEnergizerFound();
			} else {
				game.scoreSimplePelletFound();
			}
			if (game.isBonusDue()) {
				bonusControl.setState(BonusState.ACTIVE);
			}
			if (game.lives > livesBeforeScoring) {
				extraLife = true;
			}
			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
			} else if (energizer && game.level.pacManPowerSeconds > 0) {
				ghostCommand.stopAttacking();
				folks.allInWorld()
						.forEach(creature -> creature.process(new PacManGainsPowerEvent(sec(game.level.pacManPowerSeconds))));
			}
		}

		private void updateSound() {
			if (System.currentTimeMillis() - lastMealTime < 300) {
				if (!sounds.clipEating().isRunning()) {
					sounds.clipEating().loop();
				}
			} else {
				sounds.clipEating().stop();
			}
			if (folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.CHASING))) {
				if (!sounds.clipGhostChase().isRunning()) {
					sounds.clipGhostChase().loop();
				}
			} else {
				sounds.clipGhostChase().stop();
			}
			if (folks.pacMan.is(PacManState.POWERFUL)) {
				if (!sounds.clipWaza().isRunning()) {
					sounds.clipWaza().loop();
				}
			}
			if (ghostEaten) {
				sounds.clipEatGhost().play();
			}
			if (folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.DEAD))) {
				if (!sounds.clipGhostDead().isRunning()) {
					sounds.clipGhostDead().loop();
				}
			} else {
				sounds.clipGhostDead().stop();
			}
			if (extraLife) {
				sounds.clipExtraLife().play();
			}
		}
	}

	private class ChangingLevelState extends State<PacManGameState> {

		private float flashingSeconds(Theme theme, Game game) {
			if (game != null) {
				return game.level.numFlashes * theme.$float("maze-flash-sec");
			}
			return 0;
		}

		public ChangingLevelState() {
			setTimer(() -> sec(4 + flashingSeconds(theme(), game)));
		}

		@Override
		public void onEntry() {
			loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
			folks.pacMan.fallAsleep();
			doorMan.onLevelChange();
			playView.enableGhostAnimations(false);
			sounds.clips().forEach(SoundClip::stop);
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long ticksRemaining) {
			float flashingSeconds = flashingSeconds(theme(), game);

			// During first two seconds, do nothing. At second 2, hide ghosts and start flashing.
			if (passed == sec(2)) {
				folks.ghostsInWorld().forEach(ghost -> ghost.setVisible(false));
				if (flashingSeconds > 0) {
					world.setChanging(true);
				}
			}

			// After two more seconds, enter next level
			if (passed == sec(2 + flashingSeconds)) {
				game.enterLevel(game.level.number + 1);
				world.setChanging(false);
				world.fillFood();
				folks.allInWorld().forEach(Creature::init);
				playView.enableGhostAnimations(true);
				playView.init();
			}

			// Wait a second, then let ghosts jump again inside the house
			if (passed >= sec(3 + flashingSeconds)) {
				folks.allInWorld().forEach(Creature::update);
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
		world.fillFood();
		game = new Game(settings.startLevel, world.totalFoodCount());
		ghostCommand = new GhostCommand(game, folks);
		ghostCommand.init();
		bonusControl = new BonusControl(game, world);
		bonusControl.init();
		doorMan = new DoorMan(world, world.house(0), game, folks);
		folks.all().forEach(world::include);
		folks.all().forEach(Creature::init);
		folks.ghosts().forEach(ghost -> ghost.getReadyToRumble(game));
		folks.pacMan.setSpeed(() -> pacManSpeed(folks.pacMan, game.level));
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

	public void selectTheme(String themeName) {
		currentThemeIndex = range(0, themes.length).filter(i -> themes[i].name().equalsIgnoreCase(themeName)).findFirst()
				.orElse(0);
		if (currentView != null) {
			currentView.setTheme(theme());
		}
		sounds = theme().sounds();
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

	protected void setDemoMode(boolean demoMode) {
		if (demoMode) {
			settings.pacManImmortable = true;
			folks.pacMan.behavior(new SearchingForFoodAndAvoidingGhosts(folks));
			playView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
		} else {
			settings.pacManImmortable = false;
			you(folks.pacMan).followTheKeys().keys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT).ok();
			playView.clearMessage(1);
		}
	}
}