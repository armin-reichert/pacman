package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.common.Steerings.fleeingToSafeCorner;
import static de.amr.games.pacman.actor.behavior.common.Steerings.movingRandomlyNoReversing;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.START_PLAYING;
import static de.amr.games.pacman.model.PacManGame.min;
import static de.amr.games.pacman.model.PacManGame.sec;

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
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
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
public class PacManGameController extends StateMachine<PacManGameState, PacManGameEvent>
		implements VisualController {

	private PacManGame game;
	private PacManTheme theme;
	private Controller currentView;
	private PacManGameCast cast;
	private GhostMotionTimer ghostMotionTimer;
	private PlayingState playingState;
	private IntroView introView;
	private PlayView playView;
	private boolean muted = false;

	public PacManGameController(PacManTheme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		buildStateMachine();
		setIgnoreUnknownEvents(true);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		introView = new IntroView(theme);
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
						show(introView);
						introView.theme.snd_insertCoin().play();
						introView.theme.loadMusic();
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						game.start();
						cast.theme.snd_clips_all().forEach(Sound::stop);
						cast.theme.snd_ready().play();
						cast.actors().forEach(Actor::activate);
						playView.init();
						playView.textColor = Color.YELLOW;
						playView.message = "Ready!";
						playView.showScores = true;
					})
					.onTick(() -> {
						cast.activeGhosts().forEach(Ghost::update);
					})
				
				.state(START_PLAYING)
					.timeoutAfter(sec(1.7f))
					.onEntry(() -> {
						ghostMotionTimer.init();
						cast.ghosts().forEach(ghost -> ghost.foodCount = 0);
						cast.theme.music_playing().volume(.90f);
						cast.theme.music_playing().loop();
						playView.message = null;
						playView.energizerBlinking.setEnabled(true);
					})
					.onTick(() -> {
						cast.activeGhosts().forEach(Ghost::update);
					})
				
				.state(PLAYING)
					.impl(playingState = new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(2 + 0.4f * game.level.mazeNumFlashes))
					.onEntry(() -> {
						cast.theme.snd_clips_all().forEach(Sound::stop);
						cast.activeGhosts().forEach(Ghost::hide);
						cast.pacMan.sprites.select("full");
						playView.mazeFlashing = game.level.mazeNumFlashes > 0;
					})
					.onTick(() -> {
						if (state().getTicksRemaining() == sec(2)) {
							game.nextLevel();
							cast.activeActors().forEach(Actor::init);
							cast.ghosts().forEach(ghost -> ghost.foodCount = 0);
							playView.init();
						} else if (state().getTicksRemaining() < sec(2)) {
							cast.activeGhosts().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(Ghost::getDyingTime)
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(6) : sec(4))
					.onEntry(() -> {
							game.lives -= app().settings.getAsBoolean("pacMan.immortable") ? 0 : 1;
					})
					.onTick(() -> {
						if (state().getTicksConsumed() < sec(2)) {
							cast.pacMan.update(); // play dying animation
						}
						if (state().getTicksConsumed() == sec(1)) {
							cast.activeGhosts().forEach(Ghost::hide);
							cast.removeBonus();
						}
						if (game.lives == 0) {
							return;
						}
						if (state().getTicksConsumed() == sec(4)) {
							cast.activeActors().forEach(Actor::init);
							cast.theme.music_playing().loop();
							playView.init();
						}
						if (state().getTicksConsumed() > sec(4)) {
							cast.activeGhosts().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.timeoutAfter(min(1))
					.onEntry(() -> {
						LOGGER.info("Game is over");
						game.score.save();
						cast.activeGhosts().forEach(Ghost::show);
						cast.removeBonus();
						cast.theme.music_gameover().loop();
						playView.enableAnimations(false);
						playView.textColor = Color.RED;
						playView.message = "Game Over!";
					})
					.onExit(() -> {
						cast.theme.music_gameover().stop();
						playView.message = null;
					})

			.transitions()
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
					.act(this::newGame)
				
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
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
			
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
			cast.activeGhosts().forEach(Ghost::show);
			playView.init();
			playView.enableAnimations(true);
			playView.energizerBlinking.setEnabled(true);
		}

		@Override
		public void onTick() {
			ghostMotionTimer.update();
			cast.pacMan.update();
			cast.bonus().ifPresent(Bonus::update);
			cast.ghosts().forEach(ghost -> ghost.nextState = ghostMotionTimer.getState());
			Iterable<Ghost> ghosts = cast.activeGhosts()::iterator;
			for (Ghost ghost : ghosts) {
				if (ghost.getState() == GhostState.LOCKED && cast.canLeaveHouse(ghost)) {
					ghost.process(new GhostUnlockedEvent());
				}
				else if (ghost.getState() == GhostState.CHASING
						&& ghostMotionTimer.getState() == GhostState.SCATTERING) {
					ghost.process(new StartScatteringEvent());
				}
				else if (ghost.getState() == GhostState.SCATTERING
						&& ghostMotionTimer.getState() == GhostState.CHASING) {
					ghost.process(new StartChasingEvent());
				}
				else {
					ghost.update();
				}
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			if (e.ghost.oneOf(GhostState.CHASING, GhostState.SCATTERING)) {
				enqueue(new PacManKilledEvent(e.ghost));
			}
			else if (e.ghost.getState() == GhostState.FRIGHTENED) {
				enqueue(new GhostKilledEvent(e.ghost));
			}
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.name(), e.killer.tile()));
			game.enableGlobalFoodCounter();
			ghostMotionTimer.init();
			cast.theme.music_playing().stop();
			cast.pacMan.process(event);
			playView.energizerBlinking.setEnabled(false);
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			ghostMotionTimer.suspend();
			cast.activeActors().forEach(actor -> actor.process(event));
		}

		private void onPacManGettingWeaker(PacManGameEvent event) {
			cast.activeGhosts().forEach(ghost -> ghost.process(event));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostMotionTimer.resume();
			cast.activeGhosts().forEach(ghost -> ghost.process(event));
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.name(), e.ghost.tile()));
			cast.theme.snd_eatGhost().play();
			e.ghost.process(event);
		}

		private void onBonusFound(PacManGameEvent event) {
			cast.bonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found %s and wins %d points", bonus.symbol, bonus.value));
				boolean extraLife = game.scorePoints(bonus.value);
				cast.theme.snd_eatFruit().play();
				if (extraLife) {
					cast.theme.snd_extraLife().play();
				}
				bonus.process(event);
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			int points = game.eat(e.tile);
			boolean extraLife = game.scorePoints(points);
			cast.updateFoodCounter();
			cast.theme.snd_eatPill().play();
			if (extraLife) {
				cast.theme.snd_extraLife().play();
			}
			if (game.numPelletsRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusScoreReached()) {
				cast.addBonus();
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	/**
	 * "Ghost dying" state implementation.
	 */
	private class GhostDyingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			cast.pacMan.hide();
			int points = 200 * (int) Math.pow(2, game.level.bodyCount);
			boolean extraLife = game.scorePoints(points);
			LOGGER.info(() -> String.format("Scored %d points for killing %s ghost", points,
					new String[] { "first", "2nd", "3rd", "4th" }[game.level.bodyCount]));
			if (extraLife) {
				cast.theme.snd_extraLife().play();
			}
			game.level.bodyCount += 1;
		}

		@Override
		public void onTick() {
			cast.bonus().ifPresent(Bonus::update);
			cast.activeGhosts()
					.filter(ghost -> ghost.oneOf(GhostState.DYING, GhostState.DEAD, GhostState.ENTERING_HOUSE))
					.forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			cast.pacMan.show();
		}
	}

	// View handling

	public void setTheme(PacManTheme theme) {
		cast.setTheme(theme);
		introView = new IntroView(theme);
		playView.updateTheme();
	}

	private void show(Controller view) {
		if (this.currentView != view) {
			this.currentView = view;
			view.init();
		}
	}

	@Override
	public View currentView() {
		return (View) currentView;
	}

	// Controller methods

	private void newGame() {
		game = new PacManGame();
		cast = new PacManGameCast(game, theme);
		ghostMotionTimer = new GhostMotionTimer(game);
		cast.pacMan.addGameEventListener(this::process);
		playView = new PlayView(cast);
		playView.fnGhostAttack = ghostMotionTimer::state;
	}

	@Override
	public void update() {
		handleMuteSound();
		handleToggleStateMachineLogging();
		handlePlayingSpeedChange();
		handleToggleGhostFrightenedBehavior();
		handleToggleOverflowBug();
		handleCheats();
		super.update();
		currentView.update();
	}

	// Input

	private void handleCheats() {
		/* ALT-"K": Kill all ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			cast.activeGhosts().forEach(ghost -> ghost.process(new GhostKilledEvent(ghost)));
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			game.maze.tiles().filter(game.maze::containsPellet).forEach(game::eat);
			cast.updateFoodCounter();
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (getState() == PacManGameState.PLAYING) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.level.number + 1));
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

	private void handleToggleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			Logger log = Logger.getLogger("StateMachineLogger");
			log.setLevel(log.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + log.getLevel());
		}
	}

	private void handlePlayingSpeedChange() {
		int fps = app().clock.getFrequency();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			setClockFrequency(60);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			setClockFrequency(70);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_3)) {
			setClockFrequency(80);
		}
		else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_LEFT)) {
			if (fps > 5) {
				setClockFrequency(fps > 10 ? fps - 5 : Math.max(fps - 1, 5));
			}
		}
		else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_RIGHT)) {
			setClockFrequency(fps < 10 ? fps + 1 : fps + 5);
		}
	}

	private void setClockFrequency(int ticksPerSecond) {
		app().clock.setFrequency(ticksPerSecond);
		LOGGER.info(() -> String.format("Clock frequency set to %d ticks/sec", ticksPerSecond));
	}

	private void handleToggleGhostFrightenedBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			boolean original = app().settings.getAsBoolean("ghost.originalBehavior");
			if (original) {
				app().settings.set("ghost.originalBehavior", false);
				cast.ghosts().forEach(ghost -> ghost.setSteering(FRIGHTENED, fleeingToSafeCorner(cast.pacMan)));
				LOGGER.info(() -> "Changed ghost escape behavior to escaping via safe route");
			}
			else {
				app().settings.set("ghost.originalBehavior", true);
				cast.ghosts().forEach(ghost -> ghost.setSteering(FRIGHTENED, movingRandomlyNoReversing()));
				LOGGER.info(() -> "Changed ghost escape behavior to original random movement");
			}
		}
	}
}