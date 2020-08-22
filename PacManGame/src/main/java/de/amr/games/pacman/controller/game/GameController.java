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
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.easy.game.assets.SoundClip;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.bonus.BonusFoodController;
import de.amr.games.pacman.controller.bonus.BonusFoodState;
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
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
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

	private static class SoundState {
		boolean gotExtraLife;
		boolean ghostEaten;
		boolean bonusEaten;
		boolean pacManDied;
		boolean chasingGhosts;
		boolean deadGhosts;
		private long lastMealAt;
	}

	public final Game game;
	public final World world;

	public final Theme[] themes;
	public Theme theme;
	public int themeIndex = 0;
	public final SoundState sound;

	public PacManGameView currentView;
	public PlayView playView;

	public final Folks folks;

	public final BonusFoodController bonusControl;
	public final DoorMan doorMan;
	public final GhostCommand ghostCommand;

	public GameController(Theme... supportedThemes) {
		super(PacManGameState.class);
		buildStateMachine();

		themes = supportedThemes;
		setTheme(settings.theme);

		sound = new SoundState();

		game = new Game();
		world = new ArcadeWorld();
		folks = new Folks(world, world.house(0));

		folks.guys().forEach(guy -> {
			guy.ai.addEventListener(this::process);
			guy.game = game;
		});

		app().onClose(() -> {
			if (game.level != null) {
				game.level.hiscore.save();
			}
		});

		doorMan = new DoorMan(world, world.house(0), game, folks);
		ghostCommand = new GhostCommand(game, folks);
		//@formatter:off
		bonusControl = new BonusFoodController(game, world,
			() -> Timing.sec(Game.BONUS_SECONDS + new Random().nextFloat()),
			() -> ArcadeBonus.of(game.level.bonusSymbol, game.level.bonusValue, ArcadeWorld.BONUS_LOCATION));
		//@formatter:on
	}

	private void buildStateMachine() {
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
					.timeoutAfter(Timing.sec(1))
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
					.timeoutAfter(Timing.sec(5))
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
						if (passed == Timing.sec(2)) {
							bonusControl.setState(BonusFoodState.BONUS_INACTIVE);
							folks.ghostsInWorld().forEach(ghost -> ghost.body.visible = false);
						}
						else if (passed == Timing.sec(2.5f)) {
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
					.act(this::startBackgroundMusicForPlaying)
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

	public class GettingReadyState extends State<PacManGameState> {

		public GettingReadyState() {
			setTimer(Timing.sec(6));
		}

		@Override
		public void onEntry() {
			startGame();
			world.setFrozen(true);
			world.houses().flatMap(House::doors).forEach(doorMan::closeDoor);
			currentView = playView = createPlayView();
			playView.init();
			playView.showMessage(2, "Ready!", Color.YELLOW);
			theme.sounds().playMusic(theme.sounds().musicGameReady());
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			if (remaining == Timing.sec(1)) {
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

		final long INITIAL_WAIT_TIME = Timing.sec(2);

		@Override
		public void onEntry() {
			startBackgroundMusicForPlaying();
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
			int value = game.level.bonusValue;
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
				bonusControl.setState(BonusFoodState.BONUS_CONSUMABLE);
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
						.forEach(guy -> guy.ai.process(new PacManGainsPowerEvent(Timing.sec(game.level.pacManPowerSeconds))));
			}
		}
	}

	public class ChangingLevelState extends State<PacManGameState> {

		private boolean complete;
		private long flashingStart = Timing.sec(2), flashingEnd;

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
			flashingEnd = flashingStart + game.level.numFlashes * Timing.sec(theme.$float("maze-flash-sec"));
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
			if (passed >= flashingEnd + Timing.sec(2)) {
				folks.guysInWorld().forEach(SmartGuy::update);
			}

			if (passed == flashingEnd + Timing.sec(4)) {
				world.setFrozen(false);
				complete = true;
			}
		}
	}

	protected void startGame() {
		game.start(settings.startLevel, world);
		folks.guys().forEach(guy -> {
			world.include(guy.body);
			guy.init();
		});
		folks.blinky.madness.init();
		ghostCommand.init();
		bonusControl.init();
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
			Timing.changeClockFrequency(60);
		} else if (Keyboard.keyPressedOnce("2") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			Timing.changeClockFrequency(70);
		} else if (Keyboard.keyPressedOnce("3") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			Timing.changeClockFrequency(80);
		} else if (Keyboard.keyPressedOnce("z")) {
			themeIndex = (themeIndex + 1) % themes.length;
			theme = themes[themeIndex];
			currentView.setTheme(theme);
		}
	}

	private void startBackgroundMusicForPlaying() {
		theme.sounds().musicGameRunning().ifPresent(music -> {
			if (!music.isRunning()) {
				music.setVolume(0.4f);
				music.loop();
			}
		});
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

	public void setTheme(String themeName) {
		//@formatter:off
		themeIndex = IntStream.range(0, themes.length)
			.filter(i -> themes[i].name().equalsIgnoreCase(themeName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Illegal theme name: " + themeName));
		//@formatter:on
		theme = themes[themeIndex];
		if (currentView != null) {
			currentView.setTheme(theme);
		}
	}

	public Theme getTheme() {
		return themes[themeIndex];
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}
}