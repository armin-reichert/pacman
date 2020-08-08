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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.SoundClip;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
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
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.BonusFoodState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.api.Themeable;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.loading.MusicLoadingView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent>
		implements VisualController, Themeable {

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at
	 * level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	public static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	// model
	protected Game game;
	protected World world;

	// view
	protected final Theme[] themes;
	protected Theme theme;
	protected int currentThemeIndex = 0;
	protected PacManGameView currentView;
	protected PlayView playView;
	protected SoundState sound = new SoundState();

	// controller
	protected Folks folks;
	protected GhostCommand ghostCommand;
	protected DoorMan doorMan;
	protected BonusControl bonusControl;

	/**
	 * Creates a new game controller.
	 * 
	 * @param supportedThemes supported themes
	 */
	public GameController(Theme... supportedThemes) {
		super(PacManGameState.class);

		themes = supportedThemes;
		currentThemeIndex = 0;
		theme = themes[currentThemeIndex];

		Application.app().onClose(() -> game().ifPresent(game -> game.hiscore.save()));

		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		//@formatter:off
		beginStateMachine()
			
			.description("Game Controller")
			.initialState(LOADING_MUSIC)
			
			.states()
			
				.state(LOADING_MUSIC)
					.onEntry(() -> currentView = createMusicLoadingView())
					.onExit(() -> currentView.exit())
					
				.state(INTRO)
					.onEntry(() -> currentView = createIntroView())
					.onExit(() -> currentView.exit())
				
				.state(GETTING_READY).customState(new GettingReadyState())
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL).customState(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						folks.pacMan.setVisible(false);
						sound.ghostEaten = true;
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
						folks.blinky.getMadnessController().pacManDies();
						theme.sounds().stopMusic(theme.sounds().musicGameRunning());
						theme.sounds().clips().forEach(SoundClip::stop);
					})
					.onTick((state, passed, remaining) -> {
						if (passed == sec(1)) {
							bonusControl.setState(BonusFoodState.ABSENT);
							folks.ghostsInWorld().forEach(ghost -> ghost.setVisible(false));
						}
						else if (passed == sec(1.5f)) {
							sound.pacManDied = true;
						}
						folks.pacMan.update();
					})
					.onExit(() -> {
						world.setFrozen(false);
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						folks.ghostsInWorld().forEach(ghost -> {
							Bed bed = world.house(0).bed(0);
							ghost.init();
							ghost.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
							ghost.setWishDir(new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT);
							ghost.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView.showMessage(2, "Game Over!", Color.RED);
						theme.sounds().stopAll();
						theme.sounds().playMusic(theme.sounds().musicGameOver());
					})
					.onTick(() -> {
						folks.ghostsInWorld().forEach(Ghost::move);
					})
					.onExit(() -> {
						playView.clearMessage(2);
						theme.sounds().stopMusic(theme.sounds().musicGameOver());
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> theme.sounds().isMusicLoaded()	&& settings.skipIntro)
					.annotation("Music loaded, skipping intro")
					
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> theme.sounds().isMusicLoaded())
					.annotation("Music loaded")

				.when(INTRO).then(GETTING_READY)
					.condition(() -> currentView.isComplete())
					.annotation("Intro complete")
					
				.when(GETTING_READY).then(PLAYING)
					.onTimeout()
					.annotation("Ready to play")
				
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(state_PLAYING()::onFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(state_PLAYING()::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(state_PLAYING()::onPacManLostPower)
			
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(state_PLAYING()::onPacManGhostCollision)
			
				.when(PLAYING).then(PACMAN_DYING)	
					.on(PacManKilledEvent.class)
	
				.when(PLAYING).then(GHOST_DYING)	
					.on(GhostKilledEvent.class)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
					.act(state_PLAYING()::resumePlaying)
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
					.act(state_PLAYING()::resumePlaying)
					.annotation(() -> String.format("Lives remaining = %d, resume game", game.lives))
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space") || Keyboard.keyPressedOnce("enter"))
					.annotation("New game requested by user")
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !theme.sounds().isMusicRunning(theme.sounds().musicGameOver()))
					.annotation("Game over music finished")
							
		.endStateMachine();
		//@formatter:on
	}

	private static class SoundState {
		boolean gotExtraLife;
		boolean ghostEaten;
		boolean bonusEaten;
		boolean pacManDied;
		boolean chasingGhosts;
		boolean deadGhosts;
		private long lastMealAt;
	}

	public class GettingReadyState extends State<PacManGameState> {

		public GettingReadyState() {
			setTimer(sec(6));
		}

		@Override
		public void onEntry() {
			newGame();
			world.setFrozen(true);
			world.houses().flatMap(House::doors).forEach(doorMan::closeDoor);
			currentView = playView = createPlayView();
			playView.init();
			playView.showMessage(2, "Ready!", Color.YELLOW);
			theme.sounds().playMusic(theme.sounds().musicGameReady());
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

	public class PlayingState extends State<PacManGameState> {

		final long INITIAL_WAIT_TIME = sec(2);

		@Override
		public void onEntry() {
			theme.sounds().musicGameRunning().ifPresent(music -> {
				music.setVolume(0.4f);
				music.loop();
			});
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			folks.allInWorld().forEach(Creature::update);
			if (passed == INITIAL_WAIT_TIME) {
				folks.pacMan.wakeUp();
			}
			if (passed > INITIAL_WAIT_TIME) {
				ghostCommand.update();
				doorMan.update();
				bonusControl.update();
				if (folks.clyde.justLeftGhostHouse()) {
					folks.blinky.getMadnessController().clydeExitsHouse();
				}
				sound.chasingGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.CHASING));
				sound.deadGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.DEAD));
			}
		}

		@Override
		public void onExit() {
			theme.sounds().clips().forEach(SoundClip::stop);
			theme.sounds().stopMusic(theme.sounds().musicGameRunning());
			sound.chasingGhosts = false;
			sound.deadGhosts = false;
		}

		private void resumePlaying() {
			world.setFrozen(false);
			bonusControl.init();
			ghostCommand.init();
			folks.allInWorld().forEach(Creature::init);
			playView.enableGhostAnimations(true);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostCommand.resumeAttacking();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			loginfo("%s got killed at %s", ghost.name, ghost.tileLocation());

			if (ghost.is(FRIGHTENED)) {
				int livesBefore = game.lives;
				game.scoreGhostKilled(ghost.name);
				if (game.lives > livesBefore) {
					sound.gotExtraLife = true;
				}
				ghost.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
			}

			else if (!settings.ghostsHarmless) {
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tileLocation());
				doorMan.onLifeLost();
				sound.chasingGhosts = false;
				sound.deadGhosts = false;
				folks.pacMan.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
			}
		}

		private void onBonusFound(PacManGameEvent event) {
			BonusFoundEvent bonusFound = (BonusFoundEvent) event;
			int value = game.level.bonusValue;
			loginfo("PacMan found bonus '%s'", bonusFound.food);
			int livesBefore = game.lives;
			game.score(value);
			sound.bonusEaten = true;
			if (game.lives > livesBefore) {
				sound.gotExtraLife = true;
			}
			bonusControl.process(event);
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			boolean energizer = world.hasFood(Pellet.ENERGIZER, found.location);
			world.clearFood(found.location);
			int livesBeforeScoring = game.lives;
			if (energizer) {
				game.scoreEnergizerFound();
			} else {
				game.scoreSimplePelletFound();
			}
			doorMan.onPacManFoundFood();
			if (game.isBonusDue()) {
				bonusControl.setState(BonusFoodState.PRESENT);
			}
			sound.lastMealAt = System.currentTimeMillis();
			if (game.lives > livesBeforeScoring) {
				sound.gotExtraLife = true;
			}
			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
			} else if (energizer && game.level.pacManPowerSeconds > 0) {
				ghostCommand.stopAttacking();
				folks.allInWorld()
						.forEach(creature -> creature.process(new PacManGainsPowerEvent(sec(game.level.pacManPowerSeconds))));
			}
		}
	}

	public class ChangingLevelState extends State<PacManGameState> {

		public ChangingLevelState() {
			setTimer(() -> sec(4 + game.level.numFlashes * theme.$float("maze-flash-sec")));
		}

		@Override
		public void onEntry() {
			loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
			folks.pacMan.fallAsleep();
			doorMan.onLevelChange();
			playView.enableGhostAnimations(false);
			theme.sounds().clips().forEach(SoundClip::stop);
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long ticksRemaining) {
			float flashingSeconds = game.level.numFlashes * theme.$float("maze-flash-sec");

			// During first two seconds, do nothing.

			// After 2 seconds, hide ghosts and start flashing.
			if (passed == sec(2)) {
				world.setChanging(true);
				folks.ghostsInWorld().forEach(ghost -> ghost.setVisible(false));
			}

			// After flashing has finished, enter next level
			if (passed == sec(2 + flashingSeconds)) {
				world.setChanging(false);
				world.fillFood();
				game.enterLevel(game.level.number + 1);
				folks.allInWorld().forEach(Creature::init);
				folks.blinky.getMadnessController().init();
				playView.init();
				playView.enableGhostAnimations(true);
			}

			// One second later, let ghosts jump again inside the house
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
		if (currentView == playView) {
			renderPlayViewSound();
		}
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
			theme = themes[currentThemeIndex];
			currentView.setTheme(theme);
		}
	}

	private void renderPlayViewSound() {
		// Pac-Man
		long starvingMillis = System.currentTimeMillis() - sound.lastMealAt;
		if (starvingMillis > 300) {
			theme.sounds().clipCrunching().stop();
		} else if (!theme.sounds().clipCrunching().isRunning()) {
			theme.sounds().clipCrunching().loop();
		}
		if (!folks.pacMan.is(PacManState.POWERFUL)) {
			theme.sounds().clipWaza().stop();
		} else if (!theme.sounds().clipWaza().isRunning()) {
			theme.sounds().clipWaza().loop();
		}
		if (sound.pacManDied) {
			theme.sounds().clipPacManDies().play();
			sound.pacManDied = false;
		}
		if (sound.bonusEaten) {
			theme.sounds().clipEatFruit().play();
			sound.bonusEaten = false;
		}
		if (sound.gotExtraLife) {
			theme.sounds().clipExtraLife().play();
			sound.gotExtraLife = false;
		}

		// Ghosts
		if (!sound.chasingGhosts) {
			theme.sounds().clipGhostChase().stop();
		} else if (!theme.sounds().clipGhostChase().isRunning()) {
			theme.sounds().clipGhostChase().setVolume(0.5f);
			theme.sounds().clipGhostChase().loop();
		}
		if (!sound.deadGhosts) {
			theme.sounds().clipGhostDead().stop();
		} else if (!theme.sounds().clipGhostDead().isRunning()) {
			theme.sounds().clipGhostDead().loop();
		}
		if (sound.ghostEaten) {
			theme.sounds().clipEatGhost().play();
			sound.ghostEaten = false;
		}
	}

	protected void newGame() {
		world.fillFood();
		game = new Game(settings.startLevel, world.totalFoodCount());
		ghostCommand = new GhostCommand(game, folks);
		bonusControl = new BonusControl(game, world);
		doorMan = new DoorMan(world, world.house(0), game, folks);
		folks.all().forEach(world::include);
		folks.getReadyToRumble(game);
	}

	protected MusicLoadingView createMusicLoadingView() {
		return new MusicLoadingView(theme);
	}

	protected IntroView createIntroView() {
		return new IntroView(theme);
	}

	protected PlayView createPlayView() {
		return new PlayView(world, theme, folks, game, ghostCommand, doorMan);
	}

	/**
	 * Returns the typed PLAYING-state such that method references can be used. I found the expression
	 * {@code this.<PlayingState>state(PLAYING)} too ugly.
	 */
	protected PlayingState state_PLAYING() {
		return state(PLAYING);
	}

	public Folks folks() {
		return folks;
	}

	public void selectTheme(String themeName) {
		currentThemeIndex = IntStream.range(0, themes.length).filter(i -> themes[i].name().equalsIgnoreCase(themeName))
				.findFirst().orElse(0);
		setTheme(themes[currentThemeIndex]);
	}

	@Override
	public void setTheme(Theme theme) {
		this.currentThemeIndex = Arrays.asList(themes).indexOf(theme);
		this.theme = theme;
		if (currentView != null) {
			currentView.setTheme(theme);
		}
	}

	@Override
	public Theme getTheme() {
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

}