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
import java.util.function.Consumer;
import java.util.logging.Level;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
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
import de.amr.games.pacman.view.play.PlayViewXtended;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The main controller of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameController extends StateMachine<GameState, GameEvent> implements ViewController {

	private final PacManGame game;
	private final GhostAttackController ghostAttackController;
	private final PacManTheme theme;

	public PacManGameController() {
		super(GameState.class);
		game = new PacManGame();
		buildStateMachine();
		traceTo(LOGGER, app().clock::getFrequency);
		ghostAttackController = new GhostAttackController(game);
		ghostAttackController.traceTo(LOGGER, app().clock::getFrequency);
		game.pacMan.getEventManager().addListener(this::process);
		game.ghosts().forEach(ghost -> ghost.fnNextState = ghostAttackController::getState);
		theme = app().settings.get("theme");
	}

	// View controllers ("scenes")

	private IntroView introView;

	private IntroView getIntroView() {
		if (introView == null) {
			introView = new IntroView();
		}
		return introView;
	}

	private PlayViewXtended playView;

	private PlayView getPlayView() {
		if (playView == null) {
			playView = new PlayViewXtended(game);
		}
		return playView;
	}

	private Controller currentView;

	private void setViewController(Controller controller) {
		if (currentView != controller) {
			currentView = controller;
			currentView.init();
		}
	}

	@Override
	public View currentView() {
		return (View) currentView;
	}

	@Override
	public void update() {
		checkLoggingChange();
		checkNextLevelCheat();
		checkSpeedChange();
		super.update();
		currentView.update();
	}

	private void checkNextLevelCheat() {
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (getState() == GameState.PLAYING) {
				enqueue(new LevelCompletedEvent());
			}
		}
	}

	private void checkLoggingChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			if (LOGGER.getLevel() == Level.OFF) {
				LOGGER.setLevel(Level.INFO);
				LOGGER.info("Logging enabled");
			}
			else {
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

	private void foreachActiveGhost(Consumer<Ghost> action) {
		game.activeGhosts().forEach(action::accept);
	}

	/* Typed access to playing state object */
	private PlayingState playingState() {
		return state(PLAYING);
	}

	private boolean isPacManDead() {
		return game.pacMan.getState() == PacManState.DEAD;
	}

	private void resetPlayView() {
		game.initActiveActors();
		getPlayView().init();
	}

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						setViewController(getIntroView());
						theme.snd_insertCoin().play();
						theme.loadMusic();
					})
				
				.state(READY)
					.impl(new ReadyState())
				
				.state(PLAYING)
					.impl(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.impl(new ChangingLevelState())
					.timeoutAfter(() -> app().clock.sec(3))
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(game::getGhostDyingTime)
				
				.state(PACMAN_DYING)
					.impl(new PacManDyingState())
				
				.state(GAME_OVER)
					.impl(new GameOverState())
					.timeoutAfter(() -> app().clock.sec(30))
	
			.transitions()
			
				.when(INTRO).then(READY)
					.condition(() -> getIntroView().isComplete())
					.act(() -> setViewController(getPlayView()))
				
				.when(READY).then(PLAYING)
					.onTimeout()
					.act(() -> playingState().setInitialWaitTimer(app().clock.sec(1.7f)))
					
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
			
				.stay(CHANGING_LEVEL)
					.on(PacManLostPowerEvent.class)
				
				.stay(GHOST_DYING)
					.on(PacManGettingWeakerEvent.class)
				
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.condition(() -> isPacManDead() && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> isPacManDead() && game.getLives() > 0)
					.act(() -> {
						resetPlayView();
						playingState().setInitialWaitTimer(app().clock.sec(1.7f));
					})
			
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
			resetPlayView();
			getPlayView().setScoresVisible(true);
			getPlayView().enableAnimation(false);
			getPlayView().showInfoText("Ready!", Color.YELLOW);
			theme.snd_clips_all().forEach(Sound::stop);
			theme.snd_ready().play();
		}

		@Override
		public void onExit() {
			getPlayView().enableAnimation(true);
			getPlayView().hideInfoText();
			theme.music_playing().volume(0.5f);
			theme.music_playing().loop();
		}
	}

	private class PlayingState extends State<GameState, GameEvent> {

		private int initialWaitTimer;

		public void setInitialWaitTimer(int ticks) {
			initialWaitTimer = ticks;
		}

		@Override
		public void onEntry() {
			ghostAttackController.init();
			foreachActiveGhost(ghost -> {
				ghost.setVisible(true);
			});
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
				foreachActiveGhost(Ghost::update);
			}
			game.pacMan.update();
		}

		private void fireAttackStateChange() {
			switch (ghostAttackController.getState()) {
			case CHASING:
				foreachActiveGhost(ghost -> ghost.processEvent(new StartChasingEvent()));
				break;
			case SCATTERING:
				foreachActiveGhost(ghost -> ghost.processEvent(new StartScatteringEvent()));
				break;
			default:
				break;
			}
		}

		private void onPacManGhostCollision(GameEvent event) {
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

		private void onPacManKilled(GameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
			game.enableGlobalFoodCounter();
			game.pacMan.processEvent(e);
		}

		private void onPacManGainsPower(GameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			game.pacMan.processEvent(e);
			foreachActiveGhost(ghost -> ghost.processEvent(e));
			ghostAttackController.suspend();
		}

		private void onPacManGettingWeaker(GameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			foreachActiveGhost(ghost -> ghost.processEvent(e));
		}

		private void onPacManLostPower(GameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			foreachActiveGhost(ghost -> ghost.processEvent(e));
			ghostAttackController.resume();
		}

		private void onGhostKilled(GameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
			theme.snd_eatGhost().play();
			e.ghost.processEvent(e);
		}

		private void onBonusFound(GameEvent event) {
			getPlayView().getBonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found bonus %s of value %d", bonus.symbol(), bonus.value()));
				theme.snd_eatFruit().play();
				bonus.consume();
				boolean extraLife = game.addPoints(bonus.value());
				if (extraLife) {
					theme.snd_extraLife().play();
				}
				getPlayView().setBonusTimer(app().clock.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			theme.snd_eatPill().play();
			int points = game.eatFoodAtTile(e.tile);
			boolean extraLife = game.addPoints(points);
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			if (game.getFoodRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusReached()) {
				getPlayView().setBonus(game.getBonusSymbol(), game.getBonusValue());
				getPlayView().setBonusTimer(game.getBonusTime());
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	private class ChangingLevelState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			game.pacMan.setFullSprite();
			foreachActiveGhost(ghost -> ghost.setVisible(false));
			getPlayView().setMazeFlashing(true);
			theme.snd_clips_all().forEach(Sound::stop);
		}

		@Override
		public void onTick() {
			boolean timeForChange = getTicksRemaining() == getDuration() / 2;
			if (timeForChange) {
				game.nextLevel();
				resetPlayView();
				foreachActiveGhost(ghost -> ghost.setVisible(true));
				getPlayView().showInfoText("Ready!", Color.YELLOW);
				getPlayView().setMazeFlashing(false);
				getPlayView().enableAnimation(false);
			}
		}

		@Override
		public void onExit() {
			getPlayView().hideInfoText();
			getPlayView().enableAnimation(true);
		}
	}

	private class GhostDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			game.pacMan.setVisible(false);
			boolean extraLife = game.addPoints(game.getKilledGhostValue());
			if (extraLife) {
				theme.snd_extraLife().play();
			}
			LOGGER.info(() -> String.format("Scored %d points for killing ghost #%d", game.getKilledGhostValue(),
					game.getGhostsKilledByEnergizer()));
		}

		@Override
		public void onTick() {
			game.activeGhosts()
					.filter(ghost -> ghost.getState() == GhostState.DYING || ghost.getState() == GhostState.DEAD)
					.forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.pacMan.setVisible(true);
		}
	}

	private class PacManDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			theme.music_playing().stop();
		}

		@Override
		public void onTick() {
			game.pacMan.update();
		}

		@Override
		public void onExit() {
			if (game.getLives() > 0) {
				game.removeLife();
				theme.music_playing().loop();
			}
		}
	}

	private class GameOverState extends State<GameState, GameEvent> {

		private int waitTimer;

		@Override
		public void onEntry() {
			waitTimer = app().clock.sec(3);
			game.score.save();
			foreachActiveGhost(ghost -> ghost.setVisible(true));
			getPlayView().getBonus().ifPresent(bonus -> bonus.setVisible(false));
			getPlayView().enableAnimation(false);
		}

		@Override
		public void onTick() {
			if (waitTimer > 0) {
				waitTimer -= 1;
				if (waitTimer == 0) {
					getPlayView().showInfoText("Game Over!", Color.RED);
					theme.music_gameover().loop();
				}
			}
		}

		@Override
		public void onExit() {
			getPlayView().hideInfoText();
			theme.music_gameover().stop();
		}
	}
}