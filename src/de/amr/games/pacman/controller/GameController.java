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
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Cast;
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
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
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
public class GameController extends StateMachine<GameState, GameEvent> implements ViewController {

	private final Game game;
	private final Cast actors;
	private final ScatterChaseController scatterChaseController;

	public GameController() {
		super(GameState.class);
		game = new Game(new Maze(Assets.text("maze.txt")));
		actors = new Cast(game);
		actors.pacMan.traceTo(LOGGER);
		actors.pacMan.subscribe(this::process);
		actors.getGhosts().forEach(ghost -> ghost.traceTo(LOGGER));
		buildStateMachine();
		traceTo(LOGGER, app().clock::getFrequency);
		scatterChaseController = new ScatterChaseController(this);
		scatterChaseController.traceTo(LOGGER, app().clock::getFrequency);
	}

	// Views

	private View currentView;
	private IntroView introView;
	private PlayViewX playView;

	private IntroView getIntroView() {
		if (introView == null) {
			introView = new IntroView();
		}
		return introView;
	}

	private PlayView getPlayView() {
		if (playView == null) {
			playView = new PlayViewX(game);
			playView.setActors(actors);
			actors.pacMan.setWorld(playView);
		}
		return playView;
	}

	private void setCurrentView(View view) {
		if (currentView != view) {
			currentView = view;
			((Controller) currentView).init();
		}
	}

	@Override
	public View currentView() {
		return currentView;
	}

	@Override
	public void update() {
		super.update();
		((Controller) currentView).update();
	}

	// allow typed access to state methods during construction of state machine
	private PlayingState playingState() {
		return state(PLAYING);
	}

	private void buildStateMachine() {
		//@formatter:off
		define()
			
			.description("[GameControl]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						setCurrentView(getIntroView());
						THEME.snd_insertCoin().play();
					})
					.onExit(() -> {
						THEME.snd_allSounds().forEach(Sound::stop);
					})
				
				.state(READY)
					.impl(new ReadyState())
					.timeoutAfter(() -> app().clock.sec(4.5f))
				
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
	
			.transitions()
			
				.when(INTRO).then(READY)
					.condition(() -> introView.isComplete())
					.act(() -> setCurrentView(getPlayView()))
				
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
					.condition(() -> actors.pacMan.getState() == PacManState.DEAD && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> actors.pacMan.getState() == PacManState.DEAD && game.getLives() > 0)
					.act(() -> { playView.init(); actors.init(); scatterChaseController.init(); })
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
							
		.endStateMachine();
		//@formatter:on
	}

	private class ReadyState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			game.init();
			game.removeLife();
			actors.init();
			playView.init();
			playView.setScoresVisible(true);
			playView.enableAnimation(false);
			playView.showInfoText("Ready!", Color.YELLOW);
			THEME.snd_ready().play();
		}

		@Override
		public void onExit() {
			playView.enableAnimation(true);
			playView.hideInfoText();
		}
	}

	private class PlayingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			scatterChaseController.init();
			THEME.snd_waza().loop();
		}

		@Override
		public void onExit() {
			THEME.snd_waza().stop();
		}

		@Override
		public void onTick() {
			scatterChaseController.update();
			actors.pacMan.update();
			actors.getActiveGhosts().forEach(Ghost::update);
		}

		private void onStartChasing(GameEvent event) {
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(event));
		}

		private void onStartScattering(GameEvent event) {
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(event));
		}

		private void onPacManGhostCollision(GameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			PacManState pacManState = actors.pacMan.getState();
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
			actors.pacMan.processEvent(e);
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
		}

		private void onPacManGainsPower(GameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			actors.pacMan.processEvent(e);
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManGettingWeaker(GameEvent event) {
			PacManGettingWeakerEvent e = (PacManGettingWeakerEvent) event;
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManLostPower(GameEvent event) {
			PacManLostPowerEvent e = (PacManLostPowerEvent) event;
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onGhostKilled(GameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			e.ghost.processEvent(e);
			THEME.snd_eatGhost().play();
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}

		private void onBonusFound(GameEvent event) {
			playView.getBonus().ifPresent(bonus -> {
				LOGGER.info(
						() -> String.format("PacMan found bonus %s of value %d", bonus.getSymbol(), bonus.getValue()));
				THEME.snd_eatFruit().play();
				bonus.setHonored();
				game.score.add(bonus.getValue());
				playView.setBonusTimer(app().clock.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			THEME.snd_eatPill().play();
			int lives = game.getLives();
			game.eatFoodAtTile(e.tile);
			if (lives < game.getLives()) {
				THEME.snd_extraLife().play();
			}
			if (game.allFoodEaten()) {
				enqueue(new LevelCompletedEvent());
			} else {
				if (e.energizer) {
					enqueue(new PacManGainsPowerEvent());
				}
				if (game.isBonusReached()) {
					playView.setBonus(game.getBonusSymbol(), game.getBonusValue());
					playView.setBonusTimer(game.getBonusTime());
				}
			}
		}
	}

	private class ChangingLevelState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			actors.pacMan.setFullSprite();
			actors.getActiveGhosts().forEach(ghost -> ghost.setVisible(false));
			playView.setMazeFlashing(true);
		}

		@Override
		public void onTick() {
			boolean timeForChange = getRemaining() == getDuration() / 2;
			if (timeForChange) {
				game.nextLevel();
				actors.init();
				actors.getActiveGhosts().forEach(ghost -> ghost.setVisible(true));
				playView.init();
				playView.showInfoText("Ready!", Color.YELLOW);
				playView.setMazeFlashing(false);
				playView.enableAnimation(false);
			}
		}

		@Override
		public void onExit() {
			playView.hideInfoText();
			playView.enableAnimation(true);
			scatterChaseController.init();
		}
	}

	private class GhostDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			actors.pacMan.setVisible(false);
			game.score.add(game.getKilledGhostValue());
			LOGGER.info(String.format("Scored %d points for killing ghost #%d", game.getKilledGhostValue(),
					game.getGhostsKilledByEnergizer()));
		}

		@Override
		public void onTick() {
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == GhostState.DYING).forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			actors.pacMan.setVisible(true);
		}
	}

	private class PacManDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getActiveGhosts().forEach(ghost -> ghost.setVisible(false));
			THEME.snd_die().play();
		}

		@Override
		public void onTick() {
			actors.pacMan.update();
		}

		@Override
		public void onExit() {
			game.removeLife();
			actors.getActiveGhosts().forEach(ghost -> ghost.setVisible(true));
		}
	}

	private class GameOverState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			playView.enableAnimation(false);
			playView.showInfoText("Game Over!", Color.RED);
			game.score.save();
		}

		@Override
		public void onExit() {
			playView.hideInfoText();
		}
	}
}