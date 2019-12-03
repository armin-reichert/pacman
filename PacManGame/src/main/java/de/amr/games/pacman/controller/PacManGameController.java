package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.fleeingToSafeCorner;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.movingRandomly;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.START_PLAYING;
import static de.amr.games.pacman.model.PacManGame.sec;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
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
import de.amr.games.pacman.view.play.PlayView;
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
	private final GhostAttackTimer ghostAttackTimer;

	// UI
	private PacManTheme theme;
	private IntroView introView;
	private PlayView playView;
	private Controller ui;

	private boolean muted = false;

	public PacManGameController(PacManGame game) {
		super(PacManGameState.class);
		this.game = game;
		buildStateMachine();
		setIgnoreUnknownEvents(true);
		ghostAttackTimer = new GhostAttackTimer(() -> game.levelNumber);
		game.ghosts().forEach(ghost -> ghost.fnNextState = ghostAttackTimer::getState);
		game.pacMan.addListener(this::process);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		playView = new PlayView(game);
		playView.ghostAttackTimer = ghostAttackTimer;
	}

	// View handling

	public void setTheme(PacManTheme theme) {
		this.theme = theme;
		introView = new IntroView(theme);
		playView.setTheme(theme);
	}

	private void showUI(Controller ui) {
		if (this.ui != ui) {
			this.ui = ui;
			ui.init();
		}
	}

	@Override
	public View currentView() {
		return (View) ui;
	}

	// Controller methods

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

	// Input

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
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.levelNumber + 1));
				enqueue(new LevelCompletedEvent());
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
						showUI(introView);
						theme.snd_insertCoin().play();
						theme.loadMusic();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(() -> sec(5))
					.onEntry(() -> {
						theme.snd_clips_all().forEach(Sound::stop);
						theme.snd_ready().play();
						game.init();
						game.activeActors().forEach(Actor::init);
						playView.init();
						playView.showScores = true;
						playView.enableAnimation(false);
						playView.showInfoText("Ready!", Color.YELLOW);
					})
				
				.state(START_PLAYING)
					.timeoutAfter(() -> sec(1.7f))
					.onEntry(() -> {
						game.startLevel();
						playView.hideInfoText();
						playView.enableAnimation(true);
						theme.music_playing().volume(1f);
						theme.music_playing().loop();
					})
					.onTick(() -> {
						game.activeGhosts().forEach(Ghost::update);
					})
				
				.state(PLAYING)
					.impl(playingState = new PlayingState())
				
				.state(CHANGING_LEVEL)
					.impl(new ChangingLevelState())
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(Ghost::getDyingTime)
				
				.state(PACMAN_DYING)
					.onEntry(() -> {
						state().setTimerFunction(() -> game.pacMan.lives > 1 ? sec(6) : sec(4));
						state().resetTimer();
						theme.music_playing().stop();
						if (!app().settings.getAsBoolean("pacMan.immortable")) {
							game.pacMan.lives -= 1;
						}
					})
					.onTick(() -> {
						if (state().getTicksConsumed() < sec(2)) {
							game.pacMan.update();
						}
						if (state().getTicksConsumed() == sec(1)) {
							game.activeGhosts().forEach(Ghost::hide);
						}
						if (game.pacMan.lives > 0 && state().getTicksConsumed() == sec(4)) {
							game.activeActors().forEach(Actor::init);
							game.activeGhosts().forEach(Ghost::show);
							playView.init();
							theme.music_playing().loop();
						}
					})
				
				.state(GAME_OVER)
					.timeoutAfter(() -> sec(60))
					.onEntry(() -> {
						LOGGER.info("Game is over");
						game.score.save();
						game.activeGhosts().forEach(Ghost::show);
						game.bonus = null;
						playView.enableAnimation(false);
						theme.music_gameover().loop();
						playView.showInfoText("Game Over!", Color.RED);
					})
					.onExit(() -> {
						theme.music_gameover().stop();
						playView.hideInfoText();
					})

			.transitions()
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
					.act(() -> showUI(playView))
				
				.when(GETTING_READY).then(START_PLAYING)
					.onTimeout()
				
				.when(START_PLAYING).then(PLAYING)
					.onTimeout()
					
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
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.pacMan.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.pacMan.lives > 0)
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.onTimeout()
							
		.endStateMachine();
		//@formatter:on
	}

	// Classes implementing the FSM states:

	/**
	 * "Playing" state implementation.
	 */
	private class PlayingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			ghostAttackTimer.init();
			game.activeGhosts().forEach(Ghost::show);
		}

		@Override
		public void onTick() {
			game.pacMan.update();
			ghostAttackTimer.update();
			Iterable<Ghost> ghosts = game.activeGhosts()::iterator;
			for (Ghost ghost : ghosts) {
				if (ghost.getState() == GhostState.LOCKED && game.canLeaveHouse(ghost)) {
					ghost.process(new GhostUnlockedEvent());
				} else if (ghost.getState() == GhostState.CHASING && ghostAttackTimer.getState() == GhostState.SCATTERING) {
					ghost.process(new StartScatteringEvent());
				} else if (ghost.getState() == GhostState.SCATTERING && ghostAttackTimer.getState() == GhostState.CHASING) {
					ghost.process(new StartChasingEvent());
				} else {
					ghost.update();
				}
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			if (e.ghost.oneOf(GhostState.CHASING, GhostState.SCATTERING)) {
				enqueue(new PacManKilledEvent(e.ghost));
			} else if (e.ghost.getState() == GhostState.FRIGHTENED) {
				enqueue(new GhostKilledEvent(e.ghost));
			}
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.name, e.killer.tile()));
			game.enableGlobalFoodCounter();
			game.pacMan.process(e);
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			game.pacMan.process(e);
			game.activeGhosts().forEach(ghost -> ghost.process(e));
			ghostAttackTimer.suspend();
		}

		private void onPacManGettingWeaker(PacManGameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.process(e));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.process(e));
			ghostAttackTimer.resume();
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.name, e.ghost.tile()));
			theme.snd_eatGhost().play();
			e.ghost.process(e);
		}

		private void onBonusFound(PacManGameEvent event) {
			LOGGER.info(() -> String.format("PacMan found %s, value %d points", game.bonus.symbol(), game.bonus.value()));
			theme.snd_eatFruit().play();
			game.bonus.consume();
			boolean extraLife = game.scorePoints(game.bonus.value());
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			playView.setBonusTimer(sec(1));
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			theme.snd_eatPill().play();
			int points = game.eatFoodAtTile(e.tile);
			boolean extraLife = game.scorePoints(points);
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			if (game.numPelletsRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusReached()) {
				playView.setBonus(game.level().bonusSymbol, game.level().bonusValue);
				playView.setBonusTimer(sec(9 + new Random().nextFloat()));
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

		@Override
		public void onEntry() {
			theme.snd_clips_all().forEach(Sound::stop);
			game.activeGhosts().forEach(Ghost::hide);
			game.pacMan.sprites.select("full");
			int numFlashes = game.level().mazeNumFlashes;
			setTimerFunction(() -> sec(2 + 0.5f * numFlashes));
			resetTimer();
			if (numFlashes > 0) {
				playView.setMazeFlashing(true);
			}
		}

		@Override
		public void onTick() {
			if (getTicksRemaining() == sec(2)) {
				playView.setMazeFlashing(false);
				game.levelNumber += 1;
				game.activeActors().forEach(Actor::init);
				playView.init();
				game.startLevel();
			}
		}
	}

	/**
	 * "Ghost dying" state implementation.
	 */
	private class GhostDyingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			game.pacMan.hide();
			int points = 200 * (int) Math.pow(2, game.numGhostsKilledByEnergizer);
			boolean extraLife = game.scorePoints(points);
			LOGGER.info(() -> String.format("Scored %d points for killing %s ghost", points,
					new String[] { "first", "2nd", "3rd", "4th" }[game.numGhostsKilledByEnergizer]));
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			game.numGhostsKilledByEnergizer += 1;
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
}