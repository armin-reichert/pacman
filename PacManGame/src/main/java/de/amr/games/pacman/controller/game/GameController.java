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
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.SmartGuy;
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
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.api.Theme;
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
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVEL_DATA}) states that this corresponds to 100% base speed for Pac-Man
	 * at level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	public static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	/**
	 * Returns the number of ticks corresponding to the given time (in seconds) for a framerate of 60
	 * ticks/sec.
	 * 
	 * @param seconds seconds
	 * @return ticks corresponding to given number of seconds
	 */
	public static long sec(float seconds) {
		return Math.round(60 * seconds);
	}

	// model
	public final Game game;
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
	protected ArcadeBonusControl bonusControl;

	/**
	 * Creates a new game controller.
	 * 
	 * @param supportedThemes supported themes
	 */
	public GameController(Theme... supportedThemes) {
		super(PacManGameState.class);

		game = new Game();

		themes = supportedThemes;
		currentThemeIndex = 0;
		theme = themes[currentThemeIndex];

		Application.app().onClose(() -> {
			if (game.level != null) {
				game.level.hiscore.save();
			}
		});

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
						folks.pacMan.body.visible = false;
						sound.ghostEaten = true;
					})
					.onTick(() -> {
						bonusControl.update();
						folks.ghostsInWorld()
							.filter(ghost -> ghost.ai.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						folks.pacMan.body.visible = true;
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						if (!settings.pacManImmortable) {
							game.level.lives -= 1;
						}
						world.setFrozen(true);
						folks.blinky.madness.pacManDies();
						theme.sounds().stopMusic(theme.sounds().musicGameRunning());
						theme.sounds().clips().forEach(SoundClip::stop);
					})
					.onTick((state, passed, remaining) -> {
						if (passed == sec(2)) {
							bonusControl.setState(BonusState.ABSENT);
							folks.ghostsInWorld().forEach(ghost -> ghost.body.visible = false);
						}
						else if (passed == sec(2.5f)) {
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
							ghost.body.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
							ghost.body.wishDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
							ghost.ai.setState(new Random().nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
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
						world.restoreFood();
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
					.act(state_PLAYING()::onPacManFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(state_PLAYING()::onPacManFoundBonus)
					
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
					.condition(() -> state_CHANGING_LEVEL().isComplete())
					.act(state_PLAYING()::resumePlaying)
					.annotation("Level change complete")
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					.annotation("Resume playing")
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.level.lives == 0)
					.annotation("No lives left, game over")
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.level.lives > 0)
					.act(state_PLAYING()::resumePlaying)
					.annotation(() -> game.level != null ?
							String.format("Lives remaining = %d, resume game", game.level.lives) : "Lives remaining, resume game"
					)
			
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
			folks.guysInWorld().forEach(SmartGuy::update);
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
			if (settings.demoMode) {
				playView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
			} else {
				playView.clearMessage(1);
			}
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			folks.guysInWorld().forEach(SmartGuy::update);
			if (passed == INITIAL_WAIT_TIME) {
				folks.pacMan.wakeUp();
			}
			if (passed > INITIAL_WAIT_TIME) {
				ghostCommand.update();
				doorMan.update();
				bonusControl.update();
				if (folks.clyde.hasLeftHouse()) {
					folks.blinky.madness.clydeExitsHouse();
				}
				sound.chasingGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.CHASING));
				sound.deadGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.DEAD));
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
			folks.guysInWorld().forEach(SmartGuy::init);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostCommand.resumeAttacking();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;
			loginfo("%s got killed at %s", ghost.name, ghost.body.tile());

			if (folks.pacMan.movement.is(MovementType.TELEPORTING)) {
				return;
			}
			
			if (ghost.ai.is(FRIGHTENED)) {
				int livesBefore = game.level.lives;
				game.level.scoreGhostKilled();
				if (game.level.lives > livesBefore) {
					sound.gotExtraLife = true;
				}
				ghost.ai.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
			}

			else if (!settings.ghostsHarmless) {
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.body.tile());
				doorMan.onLifeLost();
				sound.chasingGhosts = false;
				sound.deadGhosts = false;
				folks.pacMan.ai.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
			}
		}

		private void onPacManFoundBonus(PacManGameEvent event) {
			BonusFoundEvent bonusFound = (BonusFoundEvent) event;
			int value = game.level.bonusValue;
			loginfo("PacMan found bonus '%s'", bonusFound.food);
			int livesBefore = game.level.lives;
			game.level.score(value);
			sound.bonusEaten = true;
			if (game.level.lives > livesBefore) {
				sound.gotExtraLife = true;
			}
			bonusControl.process(event);
		}

		private void onPacManFoodFound(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;
			boolean energizer = world.hasFood(ArcadeFood.ENERGIZER, found.location);
			world.eatFood(found.location);
			int livesBeforeScoring = game.level.lives;
			if (energizer) {
				game.level.scoreEnergizerEaten();
			} else {
				game.level.scoreSimplePelletEaten();
			}
			doorMan.onPacManFoundFood();
			if (game.level.isBonusDue()) {
				bonusControl.setState(BonusState.PRESENT);
			}
			sound.lastMealAt = System.currentTimeMillis();
			if (game.level.lives > livesBeforeScoring) {
				sound.gotExtraLife = true;
			}
			if (game.level.remainingFoodCount() == 0) {
				enqueue(new LevelCompletedEvent());
			} else if (energizer && game.level.pacManPowerSeconds > 0) {
				ghostCommand.pauseAttacking();
				folks.guysInWorld()
						.forEach(guy -> guy.ai.process(new PacManGainsPowerEvent(sec(game.level.pacManPowerSeconds))));
			}
		}
	}

	public class ChangingLevelState extends State<PacManGameState> {

		private boolean complete;
		private long flashingStart = sec(2), flashingEnd;

		public boolean isComplete() {
			return complete;
		}

		@Override
		public void onEntry() {
			loginfo("Ghosts killed in level %d: %d", game.level.number, game.level.ghostsKilled);
			world.setFrozen(true);
			folks.pacMan.fallAsleep();
			doorMan.onLevelChange();
			folks.ghosts().forEach(ghost -> ghost.enabled = false);
			theme.sounds().clips().forEach(SoundClip::stop);
			flashingEnd = flashingStart + game.level.numFlashes * sec(theme.$float("maze-flash-sec"));
			complete = false;
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long ticksRemaining) {

			// For two seconds, do nothing.

			// After wait time, hide ghosts and start flashing.
			if (passed == flashingStart) {
				world.setChanging(true);
				folks.ghosts().forEach(ghost -> ghost.body.visible = false);
			}

			if (passed == flashingEnd) {
				world.setChanging(false);
				world.restoreFood();
				game.nextLevel(world);
				folks.guys().forEach(SmartGuy::init);
				folks.blinky.madness.init();
				playView.init();
			}

			// One second later, let ghosts jump again inside the house
			if (passed >= flashingEnd + sec(2)) {
				folks.guysInWorld().forEach(SmartGuy::update);
			}

			if (passed == flashingEnd + sec(4)) {
				world.setFrozen(false);
				complete = true;
			}
		}
	}

	@Override
	public void init() {
		loginfo("Initializing game controller");
		selectTheme(settings.theme);
		world = new ArcadeWorld();
		folks = new Folks(world, world.house(0));
		folks.guys().forEach(guy -> world.include(guy.body));
		folks.pacMan.ai.addEventListener(this::process);
		folks.ghosts().forEach(ghost -> ghost.ai.addEventListener(this::process));
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
		if (!folks.pacMan.ai.is(PacManState.POWERFUL)) {
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
		game.start(settings.startLevel, world);
		ghostCommand = new GhostCommand(game, folks);
		bonusControl = new ArcadeBonusControl(game, world);
		doorMan = new DoorMan(world, world.house(0), game, folks);
		folks.guys().forEach(guy -> {
			world.include(guy.body);
			guy.game = game;
			guy.init();
		});
		folks.blinky.madness.init();
	}

	protected MusicLoadingView createMusicLoadingView() {
		return new MusicLoadingView(theme);
	}

	protected IntroView createIntroView() {
		return new IntroView(theme);
	}

	protected PlayView createPlayView() {
		return new PlayView(world, theme, folks, game);
	}

	/**
	 * Returns the typed PLAYING-state such that method references can be used. I found the expression
	 * {@code this.<PlayingState>state(PLAYING)} too ugly.
	 */
	protected PlayingState state_PLAYING() {
		return state(PLAYING);
	}

	protected ChangingLevelState state_CHANGING_LEVEL() {
		return state(CHANGING_LEVEL);
	}

	public Folks folks() {
		return folks;
	}

	public void selectTheme(String themeName) {
		currentThemeIndex = IntStream.range(0, themes.length).filter(i -> themes[i].name().equalsIgnoreCase(themeName))
				.findFirst().orElse(0);
		setTheme(themes[currentThemeIndex]);
	}

	public void setTheme(Theme theme) {
		this.currentThemeIndex = Arrays.asList(themes).indexOf(theme);
		this.theme = theme;
		if (currentView != null) {
			currentView.setTheme(theme);
		}
	}

	public Theme getTheme() {
		return themes[currentThemeIndex];
	}

	public World world() {
		return world;
	}

	public Optional<GhostCommand> ghostCommand() {
		return Optional.ofNullable(ghostCommand);
	}

	public Optional<DoorMan> doorMan() {
		return Optional.ofNullable(doorMan);
	}

	public Optional<ArcadeBonusControl> bonusControl() {
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