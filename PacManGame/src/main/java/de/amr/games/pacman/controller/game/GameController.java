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

	public final Game game;
	public final World world;
	public final Folks folks;
	public final BonusFoodController bonusController;
	public final DoorMan doorMan;
	public final GhostCommand ghostCommand;
	public final ThemeSelector themes;

	protected PacManGameView currentView;

	public GameController(Theme... supportedThemes) {
		super(PacManGameState.class);
		buildStateMachine();

		themes = new ThemeSelector(supportedThemes);
		themes.addListener(theme -> {
			if (currentView != null) {
				currentView.setTheme(theme);
			}
		});
		themes.select(settings.theme);

		game = new Game();
		world = new ArcadeWorld();
		folks = new Folks(world, world.house(0));
		folks.guys().forEach(guy -> {
			guy.ai.addEventListener(this::process);
			guy.game = game;
		});

		doorMan = new DoorMan(world, world.house(0), game, folks);
		ghostCommand = new GhostCommand(game, folks);
		//@formatter:off
		bonusController = new BonusFoodController(game, world,
			() -> Timing.sec(Game.BONUS_SECONDS + new Random().nextFloat()),
			() -> ArcadeBonus.of(game.level.bonusSymbol, game.level.bonusValue, ArcadeWorld.BONUS_LOCATION));
		//@formatter:on

		app().onClose(() -> {
			if (game.level != null) {
				game.level.hiscore.save();
			}
		});
	}

	/**
	 * @return A typed reference to the "PLAYING" state instance such that method references like
	 *         {@code state_PLAYING()::onPacManFoundFood} can be used. <br/>
	 *         The builtin expression {@code this.<PlayingState>state(PLAYING)} looked too ugly to me.
	 */
	protected PlayingState state_PLAYING() {
		return state(PLAYING);
	}

	protected ChangingLevelState state_CHANGING_LEVEL() {
		return state(CHANGING_LEVEL);
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
					.onEntry(() -> currentView = new MusicLoadingView(themes.current()))
					.onExit(() -> currentView.exit())
					
				.state(INTRO)
					.onEntry(() -> currentView = new IntroView(themes.current()))
					.onExit(() -> currentView.exit())
				
				.state(GETTING_READY).customState(new GettingReadyState())
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL).customState(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.timeoutAfter(Timing.sec(1))
					.onEntry(() -> {
						folks.pacMan.body.visible = false;
						playView().sound.ghostEaten = true;
					})
					.onTick(() -> {
						bonusController.update();
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
						themes.current().sounds().stopMusic(themes.current().sounds().musicGameRunning());
						themes.current().sounds().clips().forEach(SoundClip::stop);
					})
					.onTick((state, passed, remaining) -> {
						if (passed == Timing.sec(2)) {
							bonusController.setState(BonusFoodState.BONUS_INACTIVE);
							folks.ghostsInWorld().forEach(ghost -> ghost.body.visible = false);
						}
						else if (passed == Timing.sec(2.5f)) {
							playView().sound.pacManDied = true;
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
						playView().messages.showMessage(2, "Game Over!", Color.RED);
						themes.current().sounds().stopAll();
						themes.current().sounds().playMusic(themes.current().sounds().musicGameOver());
					})
					.onTick(() -> {
						folks.ghostsInWorld().forEach(Ghost::move);
					})
					.onExit(() -> {
						playView().messages.clearMessage(2);
						themes.current().sounds().stopMusic(themes.current().sounds().musicGameOver());
						world.restoreFood();
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> themes.current().sounds().isMusicLoaded()	&& settings.skipIntro)
					.annotation("Music loaded, skipping intro")
					
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> themes.current().sounds().isMusicLoaded())
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
					.act(state_PLAYING()::onPacManFoundFood)
					
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
					.condition(() -> !themes.current().sounds().isMusicRunning(themes.current().sounds().musicGameOver()))
					.annotation("Game over music finished")
							
		.endStateMachine();
		//@formatter:on
	}

	public class GettingReadyState extends State<PacManGameState> {

		private void startNewGame() {
			game.setGameLevel(settings.startLevel, world);
			world.setFrozen(true);
			world.houses().flatMap(House::doors).forEach(doorMan::closeDoor);
			folks.guys().forEach(guy -> {
				world.include(guy.body);
				guy.init();
			});
			folks.blinky.madness.init();
			ghostCommand.init();
			bonusController.init();
			currentView = createPlayView();
			playView().messages.showMessage(2, "Ready!", Color.YELLOW);
			themes.current().sounds().playMusic(themes.current().sounds().musicGameReady());
		}

		public GettingReadyState() {
			setTimer(Timing.sec(6));
		}

		@Override
		public void onEntry() {
			startNewGame();
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
			playView().messages.clearMessage(2);
		}
	}

	public class PlayingState extends State<PacManGameState> {

		final long INITIAL_WAIT_TIME = Timing.sec(2);

		@Override
		public void onEntry() {
			startBackgroundMusicForPlaying();
			if (settings.demoMode) {
				playView().messages.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
			} else {
				playView().messages.clearMessage(1);
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
				bonusController.update();
				if (folks.clyde.hasLeftHouse()) {
					folks.blinky.madness.clydeExitsHouse();
				}
				playView().sound.chasingGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.CHASING));
				playView().sound.deadGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.DEAD));
			}
		}

		@Override
		public void onExit() {
			themes.current().sounds().clips().forEach(SoundClip::stop);
			playView().sound.chasingGhosts = false;
			playView().sound.deadGhosts = false;
		}

		private void resumePlaying() {
			world.setFrozen(false);
			bonusController.init();
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
					playView().sound.gotExtraLife = true;
				}
				ghost.ai.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
			}

			else if (!settings.ghostsHarmless) {
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.body.tile());
				doorMan.onLifeLost();
				playView().sound.chasingGhosts = false;
				playView().sound.deadGhosts = false;
				folks.pacMan.ai.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
			}
		}

		private void onPacManFoundBonus(PacManGameEvent event) {
			int value = game.level.bonusValue;
			int livesBefore = game.level.lives;
			game.level.score(value);
			playView().sound.bonusEaten = true;
			if (game.level.lives > livesBefore) {
				playView().sound.gotExtraLife = true;
			}
			bonusController.process(event);
		}

		private void onPacManFoundFood(PacManGameEvent event) {
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
				bonusController.setState(BonusFoodState.BONUS_CONSUMABLE);
			}
			playView().sound.lastMealAt = System.currentTimeMillis();
			if (game.level.lives > livesBeforeScoring) {
				playView().sound.gotExtraLife = true;
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
			themes.current().sounds().clips().forEach(SoundClip::stop);
			flashingEnd = flashingStart + game.level.numFlashes * Timing.sec(themes.current().$float("maze-flash-sec"));
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
				playView().init();
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

	@Override
	public void update() {
		handleInput();
		super.update();
		currentView.update();
	}

	private void handleInput() {
		if (Keyboard.keyPressedOnce("1") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			Timing.changeClockFrequency(60);
		} else if (Keyboard.keyPressedOnce("2") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			Timing.changeClockFrequency(70);
		} else if (Keyboard.keyPressedOnce("3") || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			Timing.changeClockFrequency(80);
		} else if (Keyboard.keyPressedOnce("z")) {
			themes.next();
		}
	}

	private void startBackgroundMusicForPlaying() {
		themes.current().sounds().musicGameRunning().ifPresent(music -> {
			if (!music.isRunning()) {
				music.setVolume(0.4f);
				music.loop();
			}
		});
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	@SuppressWarnings("unchecked")
	protected <V extends PlayView> V playView() {
		return (V) currentView;
	}

	/**
	 * Can be overwritten by subclass.
	 * 
	 * @return the play view instance
	 */
	protected PlayView createPlayView() {
		return new PlayView(themes.current(), folks, game, world);
	}
}