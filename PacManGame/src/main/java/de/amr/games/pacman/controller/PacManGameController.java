package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.fleeingToSafeCorner;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.movingRandomly;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.READY;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.games.pacman.model.PacManGame.LevelData.MAZE_NUM_FLASHES;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.play.PlayViewXtended;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The Pac-Man game controller (finite state machine).
 * 
 * @author Armin Reichert
 */
public class PacManGameController extends StateMachine<PacManGameState, PacManGameEvent> implements ViewController {

	// Typed reference to "Playing" state object
	private PlayingState playingState;

	// Game (model)
	public final PacManGame game;

	// Controls the ghost attack waves
	private final GhostAttackController ghostAttackController;

	// UI
	private final PacManTheme theme;
	private IntroView introView;
	private PlayViewXtended playView;
	private Controller ui;

	private boolean muted = false;

	public PacManGameController(PacManGame game) {
		super(PacManGameState.class);
		this.game = game;
		this.theme = game.theme;
		buildStateMachine();
		ghostAttackController = new GhostAttackController(() -> game.level);
		game.ghosts().forEach(ghost -> ghost.fnNextState = ghostAttackController::getState);
		game.pacMan.addListener(this::process);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	// View handling

	private void showIntroView() {
		if (introView == null) {
			introView = new IntroView(game.theme);
		}
		show(introView);
	}

	private void showPlayView() {
		if (playView == null) {
			playView = new PlayViewXtended(game);
			playView.ghostAttackController = ghostAttackController;
		}
		show(playView);
	}

	private void show(Controller controller) {
		if (ui != controller) {
			ui = controller;
			controller.init();
		}
	}

	@Override
	public View currentView() {
		return (View) ui;
	}

	// Controller methods

	@Override
	public void init() {
		super.init();
		ui.init();
	}

	@Override
	public void update() {
		handleMuteSound();
		handleStateMachineLogging();
		handlePlayingSpeedChange();
		handleGhostFrightenedBehaviorChange();
		handleToggleOverflowBug();
		handleCheats();
		super.update();
		ui.update();
	}

	private void handleCheats() {
		/* ALT-"K": Kill all ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			game.activeGhosts().forEach(ghost -> ghost.process(new GhostKilledEvent(ghost)));
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			game.maze.tiles().filter(game.maze::containsPellet).forEach(game::eatFoodAtTile);
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (getState() == PacManGameState.PLAYING) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.level + 1));
				process(new LevelCompletedEvent());
			}
		}
		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			boolean immortable = app().settings.getAsBoolean("pacMan.immortable");
			app().settings.set("pacMan.immortable", !immortable);
			LOGGER.info("Pac-Man immortable = " + app().settings.getAsBoolean("pacMan.immortable"));
		}
	}

	private void handleToggleOverflowBug() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_O)) {
			app().settings.set("overflowBug", !app().settings.getAsBoolean("overflowBug"));
			LOGGER.info("Overflow bug is " + (app().settings.getAsBoolean("overflowBug") ? "on" : "off"));
		}
	}

	private void handleMuteSound() {
		if (Keyboard.keyPressedOnce(Modifier.SHIFT, KeyEvent.VK_M)) {
			muted = !muted;
			Assets.muteAll(muted);
			LOGGER.info(() -> muted ? "Sound off" : "Sound on");
		}
	}

	private void handleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			Logger smLogger = Logger.getLogger("StateMachineLogger");
			smLogger.setLevel(smLogger.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging is " + smLogger.getLevel());
		}
	}

	private void handlePlayingSpeedChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			app().clock.setFrequency(60);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			app().clock.setFrequency(80);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_3)) {
			app().clock.setFrequency(100);
		}
	}

	private void handleGhostFrightenedBehaviorChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			String property = "ghost.originalBehavior";
			app().settings.set(property, !app().settings.getAsBoolean(property));
			boolean original = app().settings.getAsBoolean(property);
			game.ghosts().forEach(ghost -> ghost.setSteering(GhostState.FRIGHTENED,
					original ? movingRandomly() : fleeingToSafeCorner(game.pacMan)));
			LOGGER.info("Changed ghost FRIGHTENED behavior to " + (original ? "original" : "escape via safe route"));
		}
	}

	// The finite state machine

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						showIntroView();
						theme.snd_insertCoin().play();
						theme.loadMusic();
					})
				
				.state(READY)
					.impl(new ReadyState())
				
				.state(PLAYING)
					.impl(playingState = new PlayingState())
				
				.state(CHANGING_LEVEL)
					.impl(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(Ghost::getDyingTime)
				
				.state(PACMAN_DYING)
					.impl(new PacManDyingState())
				
				.state(GAME_OVER)
					.impl(new GameOverState())
					.timeoutAfter(() -> app().clock.sec(60))
	
			.transitions()
			
				.when(INTRO).then(READY)
					.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
					.act(() -> showPlayView())
				
				.when(READY).then(PLAYING)
					.onTimeout()
					.act(() -> playingState.resetTimer())
					
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState::onFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(playingState::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(playingState::onPacManGhostCollision)
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(playingState::onPacManGainsPower)
					
				.stay(PLAYING)
					.on(PacManGettingWeakerEvent.class)
					.act(playingState::onPacManGettingWeaker)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(playingState::onPacManLostPower)
			
				.when(PLAYING).then(GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(playingState::onGhostKilled)
					
				.when(PLAYING).then(PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(playingState::onPacManKilled)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
					
				.stay(CHANGING_LEVEL)
					.on(PacManGettingWeakerEvent.class)
			
				.stay(CHANGING_LEVEL)
					.on(PacManLostPowerEvent.class)
				
				.stay(GHOST_DYING)
					.on(PacManGettingWeakerEvent.class)
				
				.stay(GHOST_DYING)
					.on(PacManLostPowerEvent.class)
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.condition(() -> game.pacMan.getState() == PacManState.DEAD && game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> game.pacMan.getState() == PacManState.DEAD && game.lives > 0)
					.act(() -> {
						game.activeActors().forEach(MazeMover::init);
						playView.init();
						playingState.resetTimer();
					})
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.onTimeout()
							
		.endStateMachine();
		//@formatter:on
	}

	// Classes implementing the FSM states:

	/**
	 * "Ready" state implementation.
	 */
	private class ReadyState extends State<PacManGameState, PacManGameEvent> {

		{ // just to demonstrate that timer can also be set here
			setTimerFunction(() -> app().clock.sec(4.5f));
		}

		@Override
		public void onEntry() {
			game.init();
			game.removeLife();
			game.activeActors().forEach(MazeMover::init);
			playView.init();
			playView.showScores = true;
			playView.enableAnimation(false);
			playView.showInfoText("Ready!", Color.YELLOW);
			theme.snd_clips_all().forEach(Sound::stop);
			theme.snd_ready().play();
		}

		@Override
		public void onExit() {
			playView.enableAnimation(true);
			playView.hideInfoText();
			theme.music_playing().volume(1f);
			theme.music_playing().loop();
		}
	}

	/**
	 * "Playing" state implementation.
	 */
	private class PlayingState extends State<PacManGameState, PacManGameEvent> {

		{
			setTimerFunction(() -> sec(1.7f)); // initial wait time
		}

		@Override
		public void onEntry() {
			resetTimer();
			ghostAttackController.init();
			game.activeGhosts().forEach(Ghost::show);
		}

		@Override
		public void onTick() {
			if (getTicksRemaining() > 0) {
				if (getTicksRemaining() == 1) {
					playView.hideInfoText();
				}
				return;
			}

			game.pacMan.update();
			ghostAttackController.update();
			Iterable<Ghost> ghosts = game.activeGhosts()::iterator;
			for (Ghost ghost : ghosts) {
				if (ghost.getState() == GhostState.LOCKED && game.canLeaveHouse(ghost)) {
					ghost.process(new GhostUnlockedEvent());
				} else if (ghost.getState() == GhostState.CHASING
						&& ghostAttackController.getState() == GhostState.SCATTERING) {
					ghost.process(new StartScatteringEvent());
				} else if (ghost.getState() == GhostState.SCATTERING
						&& ghostAttackController.getState() == GhostState.CHASING) {
					ghost.process(new StartChasingEvent());
				} else {
					ghost.update();
				}
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			switch (e.ghost.getState()) {
			case CHASING:
				enqueue(new PacManKilledEvent(e.ghost));
				return;
			case DEAD:
				return;
			case DYING:
				return;
			case ENTERING_HOUSE:
				return;
			case FRIGHTENED:
				enqueue(new GhostKilledEvent(e.ghost));
				return;
			case LEAVING_HOUSE:
				return;
			case LOCKED:
				return;
			case SCATTERING:
				enqueue(new PacManKilledEvent(e.ghost));
				return;
			default:
				throw new IllegalStateException();
			}
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.name, e.killer.currentTile()));
			game.enableGlobalFoodCounter();
			game.pacMan.process(e);
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			game.pacMan.process(e);
			game.activeGhosts().forEach(ghost -> ghost.process(e));
			ghostAttackController.suspend();
		}

		private void onPacManGettingWeaker(PacManGameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.process(e));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.process(e));
			ghostAttackController.resume();
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.name, e.ghost.currentTile()));
			theme.snd_eatGhost().play();
			e.ghost.process(e);
		}

		private void onBonusFound(PacManGameEvent event) {
			game.getBonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found %s, value %d points", bonus.symbol(), bonus.value()));
				theme.snd_eatFruit().play();
				bonus.consume();
				boolean extraLife = game.scorePoints(bonus.value());
				if (extraLife) {
					theme.snd_extraLife().play();
				}
				playView.setBonusTimer(app().clock.sec(1));
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			theme.snd_eatPill().play();
			int points = game.eatFoodAtTile(e.tile);
			boolean extraLife = game.scorePoints(points);
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			if (game.getFoodRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusReached()) {
				playView.setBonus(game.getLevelSymbol(), game.getBonusValue());
				playView.setBonusTimer(game.getBonusDuration());
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	/**
	 * "Level changing" state implementation.
	 */
	private class ChangingLevelState extends State<PacManGameState, PacManGameEvent> {

		int numMazeFlashes() {
			return MAZE_NUM_FLASHES.$int(game.level);
		}

		/*
		 * Set state duration such that flashing animation is executed exact number of
		 * times defined for each level. One flash takes half a second.
		 */
		{
			setTimerFunction(() -> app().clock.sec(0.5f * numMazeFlashes()));
		}

		@Override
		public void onEntry() {
			theme.snd_clips_all().forEach(Sound::stop);
			game.activeGhosts().forEach(Ghost::hide);
			game.pacMan.sprites.select("full");
			playView.hideInfoText();
			resetTimer();
			if (numMazeFlashes() > 0) {
				playView.setMazeFlashing(true);
			}
		}

		@Override
		public void onExit() {
			game.nextLevel();
			LOGGER.info("Entered game level " + game.level);
			game.activeActors().forEach(MazeMover::init);
			playView.init();
			playView.showInfoText("Ready!", Color.YELLOW);
			playingState.resetTimer();
		}
	}

	/**
	 * "Ghost dying" state implementation.
	 */
	private class GhostDyingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			game.pacMan.hide();
			boolean extraLife = game.scorePoints(game.getKilledGhostValue());
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			LOGGER.info(() -> String.format("Scored %d points for killing ghost #%d", game.getKilledGhostValue(),
					game.numGhostsKilledByCurrentEnergizer()));
		}

		@Override
		public void onTick() {
			game.activeGhosts().filter(ghost -> ghost.oneOf(GhostState.DYING, GhostState.DEAD, GhostState.ENTERING_HOUSE))
					.forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.pacMan.show();
		}
	}

	/**
	 * "Pac-Man dying" state implementation.
	 */
	private class PacManDyingState extends State<PacManGameState, PacManGameEvent> {

		private int waitTimer;

		@Override
		public void onEntry() {
			theme.music_playing().stop();
			waitTimer = app().clock.sec(1);
		}

		@Override
		public void onTick() {
			if (waitTimer > 0) {
				waitTimer -= 1;
				if (waitTimer == 0) {
					game.activeGhosts().forEach(Ghost::hide);
				}
			} else {
				game.pacMan.update();
			}
		}

		@Override
		public void onExit() {
			game.activeGhosts().forEach(Ghost::show);
			if (game.lives > 0) {
				game.removeLife();
				theme.music_playing().loop();
			}
		}
	}

	/**
	 * "Game over" state implementation.
	 */
	private class GameOverState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			LOGGER.info("Game is over");
			game.score.save();
			game.activeGhosts().forEach(Ghost::show);
			game.getBonus().ifPresent(Bonus::hide);
			playView.enableAnimation(false);
			playView.showInfoText("Game Over!", Color.RED);
			theme.music_gameover().loop();
		}

		@Override
		public void onExit() {
			playView.hideInfoText();
			theme.music_gameover().stop();
		}
	}
}