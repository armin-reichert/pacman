/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.game;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.GAME_OVER;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.GETTING_READY;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.GHOST_DYING;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.INTRO;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.LOADING_MUSIC;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacmanfsm.controller.game.PacManGameState.PLAYING;
import static de.amr.games.pacmanfsm.controller.game.Timing.sec;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.SoundClip;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.controller.bonus.BonusFoodController;
import de.amr.games.pacmanfsm.controller.bonus.BonusFoodState;
import de.amr.games.pacmanfsm.controller.creatures.Folks;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState;
import de.amr.games.pacmanfsm.controller.event.BonusFoundEvent;
import de.amr.games.pacmanfsm.controller.event.FoodFoundEvent;
import de.amr.games.pacmanfsm.controller.event.GhostKilledEvent;
import de.amr.games.pacmanfsm.controller.event.LevelCompletedEvent;
import de.amr.games.pacmanfsm.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacmanfsm.controller.event.PacManGameEvent;
import de.amr.games.pacmanfsm.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacmanfsm.controller.event.PacManKilledEvent;
import de.amr.games.pacmanfsm.controller.event.PacManLostPowerEvent;
import de.amr.games.pacmanfsm.controller.ghosthouse.DoorMan;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.game.Hiscore;
import de.amr.games.pacmanfsm.model.game.PacManGame;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeBonus;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeFood;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeWorld;
import de.amr.games.pacmanfsm.model.world.components.House;
import de.amr.games.pacmanfsm.theme.api.Theme;
import de.amr.games.pacmanfsm.view.api.PacManGameSounds;
import de.amr.games.pacmanfsm.view.api.PacManGameView;
import de.amr.games.pacmanfsm.view.intro.IntroView;
import de.amr.games.pacmanfsm.view.loading.MusicLoadingView;
import de.amr.games.pacmanfsm.view.play.PlayView;
import de.amr.statemachine.core.MissingTransitionBehavior;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite-state machine).
 * 
 * @author Armin Reichert
 */
public class GameController extends StateMachine<PacManGameState, PacManGameEvent> implements VisualController {

	private static PacManGame theGame;

	public static PacManGame theGame() {
		return theGame;
	}

	public static void newGame(int startLevel, int totalFoodCount) {
		theGame = new PacManGame(startLevel, totalFoodCount, PacManGame.PACMAN_LIVES, 0);
		theGame().hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		theGame().levelCounter.add(theGame().bonusSymbol);
		loginfo("Game started at level %d", startLevel);
	}

	public static void nextLevel() {
		if (theGame() == null) {
			throw new IllegalStateException("Cannot enter next level, game not started");
		}
		var hiscore = theGame.hiscore;
		var levelCounter = theGame.levelCounter;
		theGame = new PacManGame(theGame.level + 1, theGame.foodCount, theGame.lives, theGame.score);
		theGame.hiscore = hiscore;
		theGame.levelCounter = levelCounter;
		theGame.levelCounter.add(theGame.bonusSymbol);
		loginfo("Game entered level %d" + "", theGame.level);
	}

	public static boolean isGameStarted() {
		return theGame() != null;
	}

	public final TiledWorld world;
	public final Folks folks;
	public final BonusFoodController bonusController;
	public final DoorMan doorMan;
	public final GhostAttackController ghostCommand;
	public final ThemeSelector themes;

	protected final Random rnd = new Random();
	protected PacManGameView currentView;

	public PacManAppSettings appSettings() {
		return (PacManAppSettings) Application.app().settings();
	}

	public GameController(List<Theme> supportedThemes) {
		super(PacManGameState.class);
		buildStateMachine();

		themes = new ThemeSelector(supportedThemes);
		themes.select(appSettings().theme);
		themes.addListener(theme -> {
			if (currentView != null) {
				currentView.setTheme(theme);
			}
		});

		world = new ArcadeWorld();

		folks = new Folks(appSettings(), world, world.house(0).orElse(null));
		folks.pacMan.ai.addEventListener(this::process);
		folks.ghosts().forEach(ghost -> ghost.ai.addEventListener(this::process));

		doorMan = new DoorMan(world.house(0).orElse(null), folks);
		ghostCommand = new GhostAttackController(folks);
		bonusController = new BonusFoodController(world, () -> ArcadeBonus.of(theGame().bonusSymbol, theGame().bonusValue));

		app().onClose(() -> {
			if (isGameStarted()) {
				theGame().hiscore.save();
			}
		});
	}

	private void buildStateMachine() {
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(FoodFoundEvent.class::isInstance);
		//@formatter:off
		beginStateMachine()
			
			.description("Game Controller")
			.initialState(LOADING_MUSIC)
			
			.states()
			
				.state(LOADING_MUSIC)
					.onEntry(() -> currentView = new MusicLoadingView(appSettings(), themes.current()))
					.onExit(() -> currentView.exit())
					
				.state(INTRO)
					.onEntry(() -> currentView = new IntroView(appSettings(), themes.current()))
					.onExit(() -> currentView.exit())
				
				.state(GETTING_READY).customState(new GettingReadyState())
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL).customState(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						folks.pacMan.visible = false;
						playView().soundState.ghostEaten = true;
					})
					.onTick(() -> {
						bonusController.update();
						folks.ghostsInWorld()
							.filter(ghost -> ghost.ai.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> folks.pacMan.visible = true)
				
				.state(PACMAN_DYING)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						if (!appSettings().pacManImmortable) {
							theGame().lives -= 1;
						}
						world.setFrozen(true);
						folks.blinky.madness.pacManDies();
						sounds().stopAll();
					})
					.onTick((state, passed, remaining) -> {
						if (passed == sec(2)) {
							bonusController.setState(BonusFoodState.BONUS_INACTIVE);
							folks.ghostsInWorld().forEach(ghost -> ghost.visible = false);
						}
						else if (passed == sec(2.5f)) {
							playView().soundState.pacManDied = true;
						}
						folks.pacMan.update();
					})
					.onExit(() -> world.setFrozen(false))
				
				.state(GAME_OVER)
					.onEntry(() -> {
						closeAllDoors();
						folks.ghostsInWorld().forEach(ghost -> {
							ghost.init();
							ghost.placeAt(Tile.at(folks.blinky.bed.minX(), folks.blinky.bed.minY()), Tile.TS / 2, 0);
							ghost.wishDir = rnd.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
							ghost.ai.setState(rnd.nextBoolean() ? GhostState.SCATTERING : GhostState.FRIGHTENED);
						});
						playView().messagesView.showMessage(2, "Game Over!", Color.RED);
						sounds().stopAll();
						sounds().playMusic(sounds().musicGameOver());
					})
					.onTick(() -> folks.ghostsInWorld().forEach(Ghost::move))
					.onExit(() -> {
						world.restoreFood();
						playView().messagesView.clearMessage(2);
						sounds().stopMusic(sounds().musicGameOver());
					})
	
			.transitions()
			
				.when(LOADING_MUSIC).then(GETTING_READY)
					.condition(() -> sounds().isMusicLoaded()	&& appSettings().skipIntro)
					.annotation("Music loaded, skipping intro")
					
				.when(LOADING_MUSIC).then(INTRO)
					.condition(() -> sounds().isMusicLoaded())
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
					.act(statePlaying()::onPacManFoundFood)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(statePlaying()::onPacManFoundBonus)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(statePlaying()::onPacManLostPower)
			
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(statePlaying()::onPacManGhostCollision)
			
				.when(PLAYING).then(PACMAN_DYING)	
					.on(PacManKilledEvent.class)
	
				.when(PLAYING).then(GHOST_DYING)	
					.on(GhostKilledEvent.class)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.condition(() -> stateChangingLevel().isComplete())
					.act(statePlaying()::resumePlaying)
					.annotation("Level change complete")
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					.annotation("Resume playing")
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> theGame().lives == 0)
					.annotation("No lives left, game over")
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> theGame().lives > 0)
					.act(statePlaying()::resumePlaying)
					.annotation(() -> isGameStarted() ?
							String.format("Lives remaining = %d, resume game", theGame().lives) : "Lives remaining, resume game"
					)
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce("space") || Keyboard.keyPressedOnce("enter"))
					.annotation("New game requested by user")
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !sounds().isMusicRunning(sounds().musicGameOver()))
					.annotation("Game over music finished")
							
		.endStateMachine();
		//@formatter:on
	}

	public class GettingReadyState extends State<PacManGameState> {

		private void startNewGame() {
			newGame(appSettings().startLevel, world.totalFoodCount());
			world.setFrozen(true);
			closeAllDoors();
			folks.guys().forEach(guy -> {
				world.include(guy);
				guy.init();
			});
			folks.blinky.madness.init();
			ghostCommand.init();
			bonusController.init();
			currentView = createPlayView();
			playView().messagesView.showMessage(2, "Ready!", Color.YELLOW);
			sounds().playMusic(sounds().musicGameReady());
		}

		public GettingReadyState() {
			setTimer(sec(6));
		}

		@Override
		public void onEntry() {
			startNewGame();
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			if (remaining == sec(1)) {
				world.setFrozen(false);
			}
			folks.guysInWorld().forEach(Lifecycle::update);
		}

		@Override
		public void onExit() {
			playView().messagesView.clearMessage(2);
		}
	}

	public class PlayingState extends State<PacManGameState> {

		@Override
		public void onEntry() {
			startBackgroundMusicForPlaying();
			if (appSettings().demoMode) {
				playView().messagesView.showMessage(1, "Demo Mode", Color.LIGHT_GRAY);
			} else {
				playView().messagesView.clearMessage(1);
			}
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long remaining) {
			folks.guysInWorld().forEach(Lifecycle::update);
			if (passed == sec(2)) {
				folks.pacMan.wakeUp();
			}
			if (passed > sec(2)) {
				ghostCommand.update();
				doorMan.update();
				bonusController.update();
				if (folks.clyde.justLeftHouse()) {
					folks.blinky.madness.clydeExitsHouse();
				}
				playView().soundState.chasingGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.CHASING));
				playView().soundState.deadGhosts = folks.ghostsInWorld().anyMatch(ghost -> ghost.ai.is(GhostState.DEAD));
			}
		}

		@Override
		public void onExit() {
			sounds().clips().forEach(SoundClip::stop);
			playView().soundState.chasingGhosts = false;
			playView().soundState.deadGhosts = false;
		}

		private void resumePlaying() {
			world.setFrozen(false);
			bonusController.init();
			ghostCommand.init();
			folks.guysInWorld().forEach(Lifecycle::init);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostCommand.resumeAttacking();
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent collision = (PacManGhostCollisionEvent) event;
			Ghost ghost = collision.ghost;

			if (ghost.ai.is(FRIGHTENED)) {
				boolean extraLife = theGame().gainGhostPoints();
				playView().soundState.gotExtraLife = extraLife;
				ghost.ai.process(new GhostKilledEvent(ghost));
				enqueue(new GhostKilledEvent(ghost));
				loginfo("%s got killed at %s", ghost.name, ghost.tile());
			}

			else if (!appSettings().ghostsHarmless) {
				loginfo("Pac-Man killed by %s at %s", ghost.name, ghost.tile());
				doorMan.onPacManLostLife();
				playView().soundState.chasingGhosts = false;
				playView().soundState.deadGhosts = false;
				folks.pacMan.ai.process(new PacManKilledEvent(ghost));
				enqueue(new PacManKilledEvent(ghost));
			}
		}

		private void onPacManFoundBonus(PacManGameEvent event) {
			boolean extraLife = theGame().gainBonus();
			playView().soundState.bonusEaten = true;
			playView().soundState.gotExtraLife = extraLife;
			bonusController.process(event);
		}

		private void onPacManFoundFood(PacManGameEvent event) {
			FoodFoundEvent found = (FoodFoundEvent) event;

			boolean energizer = found.food == ArcadeFood.ENERGIZER;
			boolean extraLife = energizer ? theGame().gainEnergizerPoints() : theGame().gainPelletPoints();
			if (theGame().isBonusGettingActivated()) {
				bonusController.setState(BonusFoodState.BONUS_CONSUMABLE);
			}
			playView().soundState.lastMealAt = System.currentTimeMillis();
			playView().soundState.gotExtraLife = extraLife;

			doorMan.onPacManFoundFood();
			world.removeFood(found.location);
			if (theGame().remainingFoodCount() == 0) {
				// enter next level
				enqueue(new LevelCompletedEvent());
				return;
			}

			if (energizer && theGame().pacManPowerSeconds > 0) {
				// restart attack timer
				ghostCommand.pauseAttacking();
				PacManGameEvent pacManGainsPower = new PacManGainsPowerEvent(sec(theGame().pacManPowerSeconds));
				folks.pacMan.ai.process(pacManGainsPower);
				folks.ghostsInWorld().forEach(ghost -> ghost.ai.process(pacManGainsPower));
			}
		}
	}

	public class ChangingLevelState extends State<PacManGameState> {

		private boolean complete;
		private long flashingStart = sec(2);
		private long flashingEnd;

		public boolean isComplete() {
			return complete;
		}

		@Override
		public void onEntry() {
			loginfo("Ghosts killed in level %d: %d", theGame().level, theGame().ghostsKilledInLevel);
			world.setFrozen(true);
			folks.pacMan.fallAsleep();
			doorMan.onLevelChange();
			sounds().clips().forEach(SoundClip::stop);
			flashingEnd = flashingStart + theGame().numFlashes * sec(themes.current().asFloat("maze-flash-sec"));
			complete = false;
		}

		@Override
		public void onTick(State<PacManGameState> state, long passed, long ticksRemaining) {

			// For two seconds, do nothing.

			// After wait time, hide ghosts and start flashing.
			if (passed == flashingStart) {
				world.setChanging(true);
				folks.ghosts().forEach(ghost -> ghost.visible = false);
			}

			if (passed == flashingEnd) {
				world.setChanging(false);
				world.restoreFood();
				nextLevel();
				folks.guys().forEach(Lifecycle::init);
				folks.blinky.madness.init();
				playView().init();
			}

			// One second later, let ghosts jump again inside the house
			if (passed >= flashingEnd + sec(2)) {
				folks.guysInWorld().forEach(Lifecycle::update);
			}

			if (passed == flashingEnd + sec(4)) {
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

	protected void handleInput() {
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

	protected PacManGameSounds sounds() {
		return themes.current().sounds();
	}

	private void startBackgroundMusicForPlaying() {
		sounds().musicGameRunning().ifPresent(music -> {
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
		return new PlayView(themes.current(), folks, world);
	}

	/**
	 * @return A typed reference to the "PLAYING" state instance such that method references like
	 *         {@code state_PLAYING()::onPacManFoundFood} can be used. <br/>
	 *         The builtin expression {@code this.<PlayingState>state(PLAYING)} looked too ugly to me.
	 */
	protected PlayingState statePlaying() {
		return state(PLAYING);
	}

	protected ChangingLevelState stateChangingLevel() {
		return state(CHANGING_LEVEL);
	}

	protected void closeAllDoors() {
		world.houses().flatMap(House::doors).forEach(doorMan::closeDoor);
	}
}