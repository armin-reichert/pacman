package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.READY;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
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
public class PacManGameController extends StateMachine<PacManGameState, PacManGameEvent>
		implements ViewController {

	// Typed reference to "Playing" state object
	private PlayingState playingState;

	// Model
	private final PacManGame game;

	// Child controller
	private final GhostAttackController ghostAttackController;

	// UI
	private final PacManTheme theme;
	private IntroView introView;
	private PlayViewXtended playView;
	private Controller currentViewController;

	public PacManGameController(PacManGame game) {
		super(PacManGameState.class);
		this.game = game;
		this.theme = game.theme;
		buildStateMachine();
		ghostAttackController = new GhostAttackController(game);
		game.ghosts().forEach(ghost -> ghost.fnNextState = ghostAttackController::getState);
		game.pacMan.addListener(this::process);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	// View handling

	private void showIntroView() {
		if (introView == null) {
			introView = new IntroView(game);
		}
		show(introView);
	}

	private void showPlayView() {
		if (playView == null) {
			playView = new PlayViewXtended(game);
		}
		show(playView);
	}

	private void show(Controller controller) {
		if (currentViewController != controller) {
			currentViewController = controller;
			controller.init();
		}
	}

	@Override
	public View currentView() {
		return (View) currentViewController;
	}

	// Controller methods

	@Override
	public void update() {
		handleStateMachineLogging();
		handleNextLevelCheat();
		handlePlayingSpeedChange();
		handleGhostBehaviorChange();
		super.update();
		currentViewController.update();
	}

	private void handleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			Logger smLogger = Logger.getLogger("StateMachineLogger");
			smLogger.setLevel(smLogger.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging is " + smLogger.getLevel());
		}
	}

	private void handleNextLevelCheat() {
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (getState() == PacManGameState.PLAYING) {
				enqueue(new LevelCompletedEvent());
			}
		}
	}

	private void handlePlayingSpeedChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			app().clock.setFrequency(60);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			app().clock.setFrequency(80);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_3)) {
			app().clock.setFrequency(100);
		}
	}

	private void handleGhostBehaviorChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			game.classicFlightBehavior = !game.classicFlightBehavior;
			game.ghosts().forEach(ghost -> {
				ghost.setBehavior(GhostState.FRIGHTENED,
						game.classicFlightBehavior ? ghost.fleeingRandomly()
								: ghost.fleeingToSafeCorner(game.pacMan));
			});
			LOGGER.info("Changed ghost FRIGHTENED behavior to flee "
					+ (game.classicFlightBehavior ? "randomly" : "via safe route"));
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
					.timeoutAfter(game::getGhostDyingTime)
				
				.state(PACMAN_DYING)
					.impl(new PacManDyingState())
				
				.state(GAME_OVER)
					.impl(new GameOverState())
					.timeoutAfter(() -> app().clock.sec(60))
	
			.transitions()
			
				.when(INTRO).then(READY)
					.condition(() -> introView.isComplete())
					.act(() -> showPlayView())
				
				.when(READY).then(PLAYING)
					.onTimeout()
					.act(() -> playingState.setInitialWaitTimer(app().clock.sec(1.7f)))
					
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
					.condition(() -> game.pacMan.isDead() && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> game.pacMan.isDead() && game.getLives() > 0)
					.act(() -> {
						game.activeActors().forEach(MazeMover::init);
						playView.init();
						playingState.setInitialWaitTimer(app().clock.sec(1.7f));
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
			playView.setScoresVisible(true);
			playView.enableAnimation(false);
			playView.showInfoText("Ready!", Color.YELLOW);
			theme.snd_clips_all().forEach(Sound::stop);
			theme.snd_ready().play();
		}

		@Override
		public void onExit() {
			playView.enableAnimation(true);
			playView.hideInfoText();
			theme.music_playing().volume(0.5f);
			theme.music_playing().loop();
		}
	}

	/**
	 * "Playing" state implementation.
	 */
	private class PlayingState extends State<PacManGameState, PacManGameEvent> {

		private int initialWaitTimer;

		public void setInitialWaitTimer(int ticks) {
			initialWaitTimer = ticks;
		}

		@Override
		public void onEntry() {
			ghostAttackController.init();
			game.activeGhosts().forEach(ghost -> ghost.setVisible(true));
			fireAttackStateChange();
		}

		@Override
		public void onTick() {
			if (initialWaitTimer > 0) {
				initialWaitTimer -= 1;
				return;
			}
			GhostState oldAttackState = ghostAttackController.getState();
			ghostAttackController.update();
			if (oldAttackState != ghostAttackController.getState()) {
				fireAttackStateChange();
			}
			else {
				game.activeGhosts().forEach(Ghost::update);
			}
			game.pacMan.update();
		}

		private void fireAttackStateChange() {
			switch (ghostAttackController.getState()) {
			case CHASING:
				game.activeGhosts().forEach(ghost -> ghost.processEvent(new StartChasingEvent()));
				break;
			case SCATTERING:
				game.activeGhosts().forEach(ghost -> ghost.processEvent(new StartScatteringEvent()));
				break;
			default:
				break;
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			PacManState pacManState = game.pacMan.getState();
			if (pacManState == PacManState.DYING) {
				return;
			}
			if (pacManState == PacManState.POWER) {
				GhostState ghostState = e.ghost.getState();
				if (ghostState == GhostState.FRIGHTENED || ghostState == GhostState.CHASING
						|| ghostState == GhostState.SCATTERING) {
					enqueue(new GhostKilledEvent(e.ghost));
				}
				return;
			}
			enqueue(new PacManKilledEvent(e.ghost));
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(
					() -> String.format("PacMan killed by %s at %s", e.killer.name, e.killer.tilePosition()));
			game.enableGlobalFoodCounter();
			game.pacMan.processEvent(e);
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			game.pacMan.processEvent(e);
			game.activeGhosts().forEach(ghost -> ghost.processEvent(e));
			ghostAttackController.suspend();
		}

		private void onPacManGettingWeaker(PacManGameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManLostPower(PacManGameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			game.activeGhosts().forEach(ghost -> ghost.processEvent(e));
			ghostAttackController.resume();
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER
					.info(() -> String.format("Ghost %s killed at %s", e.ghost.name, e.ghost.tilePosition()));
			theme.snd_eatGhost().play();
			e.ghost.processEvent(e);
		}

		private void onBonusFound(PacManGameEvent event) {
			game.getBonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found bonus %s of value %d", bonus.symbol(),
						bonus.value()));
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

		{ // Set state duration such that flashing animation is executed exact number of times defined
			// for each level. One flash takes half a second.
			setTimerFunction(() -> app().clock.sec(0.5f * game.getMazeNumFlashes()));
		}

		@Override
		public void onEntry() {
			theme.snd_clips_all().forEach(Sound::stop);
			game.activeGhosts().forEach(ghost -> ghost.setVisible(false));
			game.pacMan.sprites.select("full");
			playView.hideInfoText();
			resetTimer();
			if (game.getMazeNumFlashes() > 0) {
				playView.setMazeFlashing(true);
			}
		}

		@Override
		public void onExit() {
			game.nextLevel();
			game.activeActors().forEach(MazeMover::init);
			playView.init();
			playView.showInfoText("Ready!", Color.YELLOW);
		}
	}

	/**
	 * "Ghost dying" state implementation.
	 */
	private class GhostDyingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			game.pacMan.setVisible(false);
			boolean extraLife = game.scorePoints(game.getKilledGhostValue());
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			LOGGER.info(() -> String.format("Scored %d points for killing ghost #%d",
					game.getKilledGhostValue(), game.numGhostsKilledByCurrentEnergizer()));
		}

		@Override
		public void onTick() {
			game.activeGhosts()
					.filter(
							ghost -> ghost.getState() == GhostState.DYING || ghost.getState() == GhostState.DEAD)
					.forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.pacMan.setVisible(true);
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
					game.activeGhosts().forEach(ghost -> ghost.setVisible(false));
				}
			}
			else {
				game.pacMan.update();
			}
		}

		@Override
		public void onExit() {
			game.activeGhosts().forEach(ghost -> ghost.setVisible(true));
			if (game.getLives() > 0) {
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
			game.score.save();
			game.activeGhosts().forEach(ghost -> ghost.setVisible(true));
			game.getBonus().ifPresent(bonus -> bonus.setVisible(false));
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