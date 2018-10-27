package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.GameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameState.GAME_OVER;
import static de.amr.games.pacman.controller.GameState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameState.PLAYING;
import static de.amr.games.pacman.controller.GameState.READY;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
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
import de.amr.games.pacman.view.play.PlayViewX;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The main controller of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameController extends StateMachine<GameState, GameEvent> implements ViewController {

	private final PacManGame game;
	private final GhostAttackTimer ghostAttackTimer;

	public PacManGameController() {
		super(GameState.class);
		game = new PacManGame();
		game.getPacMan().getEventManager().addListener(this::process);
		ghostAttackTimer = new GhostAttackTimer(game, this);
		game.getGhosts().forEach(ghost -> ghost.fnNextAttackState = ghostAttackTimer::getState);
		buildStateMachine();
		traceTo(LOGGER, app().clock::getFrequency);
	}

	public PacManGame getGame() {
		return game;
	}

	public PacManTheme getTheme() {
		return app().settings.get("theme");
	}

	// Screens

	private Controller currentScreen;
	private IntroView introScreen;
	private PlayViewX playScreen;

	private IntroView getIntroScreen() {
		if (introScreen == null) {
			introScreen = new IntroView();
		}
		return introScreen;
	}

	private PlayView getPlayScreen() {
		if (playScreen == null) {
			playScreen = new PlayViewX(game);
		}
		return playScreen;
	}

	private void setScreen(Controller screen) {
		if (currentScreen != screen) {
			currentScreen = screen;
			currentScreen.init();
		}
	}

	@Override
	public View currentView() {
		return (View) currentScreen;
	}

	@Override
	public void update() {
		checkLoggingChange();
		checkSpeedChange();
		super.update();
		currentScreen.update();
	}

	private void checkLoggingChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			if (LOGGER.getLevel() == Level.OFF) {
				LOGGER.setLevel(Level.INFO);
				LOGGER.info("Logging enabled");
			} else {
				LOGGER.info("Logging disabled");
				LOGGER.setLevel(Level.OFF);
			}
		}
	}

	private void checkSpeedChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			app().clock.setFrequency(60);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			app().clock.setFrequency(80);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_3)) {
			app().clock.setFrequency(100);
		}
	}

	/* Typed accessor for playing state (needed for method references etc.) */
	private PlayingState playingState() {
		return state(PLAYING);
	}

	private boolean isPacManDead() {
		return game.getPacMan().getState() == PacManState.DEAD;
	}

	private void resetPlayScreen() {
		game.initActiveActors();
		getPlayScreen().init();
	}

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[Game]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						setScreen(getIntroScreen());
						getTheme().snd_insertCoin().play();
						getTheme().loadMusic();
					})
				
				.state(READY)
					.impl(new ReadyState())
				
				.state(PLAYING)
					.impl(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.impl(new ChangingLevelState())
					.timeoutAfter(game::getLevelChangingTime)
				
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
					.condition(() -> getIntroScreen().isComplete())
					.act(() -> setScreen(getPlayScreen()))
				
				.when(READY).then(PLAYING).onTimeout()
					
				.stay(PLAYING)
					.on(StartChasingEvent.class)
					.act(playingState()::onStartChasing)
					
				.stay(PLAYING)
					.on(StartScatteringEvent.class)
					.act(playingState()::onStartScattering)
					
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(playingState()::onFoodFound)
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(playingState()::onBonusFound)
					
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(playingState()::onPacManGhostCollision)
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(playingState()::onPacManGainsPower)
					
				.stay(PLAYING)
					.on(PacManGettingWeakerEvent.class)
					.act(playingState()::onPacManGettingWeaker)
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(playingState()::onPacManLostPower)
			
				.when(PLAYING).then(GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(playingState()::onGhostKilled)
					
				.when(PLAYING).then(PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(playingState()::onPacManKilled)
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
					
				.stay(CHANGING_LEVEL)
					.on(PacManGettingWeakerEvent.class)
			
				.stay(GHOST_DYING)
					.on(PacManGettingWeakerEvent.class)
				
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.condition(() -> isPacManDead() && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> isPacManDead() && game.getLives() > 0)
					.act(this::resetPlayScreen)
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.onTimeout()
							
		.endStateMachine();
		//@formatter:on
	}

	private class ReadyState extends State<GameState, GameEvent> {

		// just to demonstrate that timer can also be set here
		{
			setTimer(() -> app().clock.sec(4.5f));
		}

		@Override
		public void onEntry() {
			game.init();
			game.removeLife();
			resetPlayScreen();
			getPlayScreen().setScoresVisible(true);
			getPlayScreen().enableAnimation(false);
			getPlayScreen().showInfoText("Ready!", Color.YELLOW);
			getTheme().snd_clips_all().forEach(Sound::stop);
			getTheme().snd_ready().play();
		}

		@Override
		public void onExit() {
			getPlayScreen().enableAnimation(true);
			getPlayScreen().hideInfoText();
			getTheme().music_playing().volume(0.5f);
			getTheme().music_playing().loop();
		}
	}

	private class PlayingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			ghostAttackTimer.init();
		}

		@Override
		public void onExit() {
		}

		@Override
		public void onTick() {
			ghostAttackTimer.update();
			game.getPacMan().update();
			game.getActiveGhosts().forEach(Ghost::update);
		}

		private void onStartChasing(GameEvent event) {
			game.getActiveGhosts().forEach(ghost -> ghost.processEvent(event));
		}

		private void onStartScattering(GameEvent event) {
			game.getActiveGhosts().forEach(ghost -> ghost.processEvent(event));
		}

		private void onPacManGhostCollision(GameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			PacManState pacManState = game.getPacMan().getState();
			if (pacManState == PacManState.DYING) {
				return;
			}
			if (pacManState == PacManState.GREEDY) {
				GhostState ghostState = e.ghost.getState();
				if (ghostState == GhostState.FRIGHTENED || ghostState == GhostState.CHASING
						|| ghostState == GhostState.SCATTERING) {
					enqueue(new GhostKilledEvent(e.ghost));
				}
				return;
			}
			enqueue(new PacManKilledEvent(e.ghost));
		}

		private void onPacManKilled(GameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
			game.getPacMan().processEvent(e);
		}

		private void onPacManGainsPower(GameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			game.getPacMan().processEvent(e);
			game.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManGettingWeaker(GameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			game.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManLostPower(GameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			game.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onGhostKilled(GameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
			getTheme().snd_eatGhost().play();
			e.ghost.processEvent(e);
		}

		private void onBonusFound(GameEvent event) {
			getPlayScreen().getBonus().ifPresent(bonus -> {
				LOGGER.info(
						() -> String.format("PacMan found bonus %s of value %d", bonus.getSymbol(), bonus.getValue()));
				getTheme().snd_eatFruit().play();
				bonus.setHonored();
				boolean extraLife = game.addPoints(bonus.getValue());
				if (extraLife) {
					getTheme().snd_extraLife().play();
				}
				getPlayScreen().setBonusTimer(app().clock.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			getTheme().snd_eatPill().play();
			int points = game.eatFoodAtTile(e.tile);
			boolean extraLife = game.addPoints(points);
			if (extraLife) {
				getTheme().snd_extraLife().play();
			}
			if (game.allFoodEaten()) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusReached()) {
				getPlayScreen().setBonus(game.getBonusSymbol(), game.getBonusValue());
				getPlayScreen().setBonusTimer(game.getBonusTime());
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	private class ChangingLevelState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			game.getPacMan().setFullSprite();
			game.getActiveGhosts().forEach(ghost -> ghost.setVisible(false));
			getPlayScreen().setMazeFlashing(true);
			getTheme().snd_clips_all().forEach(Sound::stop);
		}

		@Override
		public void onTick() {
			boolean timeForChange = getTicksRemaining() == getDuration() / 2;
			if (timeForChange) {
				game.nextLevel();
				resetPlayScreen();
				game.getActiveGhosts().forEach(ghost -> ghost.setVisible(true));
				getPlayScreen().showInfoText("Ready!", Color.YELLOW);
				getPlayScreen().setMazeFlashing(false);
				getPlayScreen().enableAnimation(false);
			}
		}

		@Override
		public void onExit() {
			getPlayScreen().hideInfoText();
			getPlayScreen().enableAnimation(true);
		}
	}

	private class GhostDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			game.getPacMan().setVisible(false);
			boolean extraLife = game.addPoints(game.getKilledGhostValue());
			if (extraLife) {
				getTheme().snd_extraLife().play();
			}
			LOGGER.info(() -> String.format("Scored %d points for killing ghost #%d", game.getKilledGhostValue(),
					game.getGhostsKilledByEnergizer()));
		}

		@Override
		public void onTick() {
			game.getActiveGhosts()
					.filter(ghost -> ghost.getState() == GhostState.DYING || ghost.getState() == GhostState.DEAD)
					.forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.getPacMan().setVisible(true);
		}
	}

	private class PacManDyingState extends State<GameState, GameEvent> {

		@Override
		public void onTick() {
			game.getPacMan().update();
		}

		@Override
		public void onExit() {
			game.removeLife();
			getGame().getActiveGhosts().forEach(ghost -> ghost.setVisible(true));
		}
	}

	private class GameOverState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			getPlayScreen().enableAnimation(false);
			getPlayScreen().showInfoText("Game Over!", Color.RED);
			game.saveScore();
			getTheme().music_playing().stop();
			getTheme().music_gameover().loop();
		}

		@Override
		public void onExit() {
			getPlayScreen().hideInfoText();
			getTheme().music_gameover().stop();
		}
	}
}