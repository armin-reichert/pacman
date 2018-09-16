package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.PacManApp.THEME;
import static de.amr.games.pacman.controller.GameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameState.GAME_OVER;
import static de.amr.games.pacman.controller.GameState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameState.PLAYING;
import static de.amr.games.pacman.controller.GameState.READY;

import java.awt.Color;
import java.awt.EventQueue;
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
	private final GhostAttackTimer ghostAttackTimer;

	public GameController() {
		super(GameState.class);
		game = new Game(new Maze(Assets.text("maze.txt")));
		actors = new Cast(game);
		actors.pacMan.getEventManager().subscribe(this::process);
		buildStateMachine();
		ghostAttackTimer = new GhostAttackTimer(this);
	}

	public Game getGame() {
		return game;
	}

	public Cast getActors() {
		return actors;
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
			playView = new PlayViewX(game, actors);
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
	public void init() {
		super.init();
		traceTo(LOGGER, app().clock::getFrequency);
		LOGGER.info("Loading audio clips...");
		THEME.snd_clips_all();
		LOGGER.info("Audio clips loaded.");
		// A trick to load the background music without delay during the intro animation
		EventQueue.invokeLater(() -> {
			LOGGER.info("Loading background music...");
			THEME.snd_music_all();
			LOGGER.info("Background music loaded.");
		});
	}

	@Override
	public void update() {
		super.update();
		((Controller) currentView).update();
	}

	// typed access to playing state implementation (needed for method references etc.)
	private PlayingState playingState() {
		return state(PLAYING);
	}

	private boolean isPacManDead() {
		return actors.pacMan.getState() == PacManState.DEAD;
	}

	private void resetScene() {
		playView.init();
		actors.init();
		ghostAttackTimer.init();
	}

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[Game]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						setCurrentView(getIntroView());
						THEME.snd_insertCoin().play();
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
					.condition(() -> isPacManDead() && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> isPacManDead() && game.getLives() > 0)
					.act(this::resetScene)
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
							
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
			resetScene();
			playView.setScoresVisible(true);
			playView.enableAnimation(false);
			playView.showInfoText("Ready!", Color.YELLOW);
			THEME.snd_clips_all().forEach(Sound::stop);
			THEME.snd_music_all().forEach(Sound::stop);
			THEME.snd_ready().play();
		}

		@Override
		public void onExit() {
			playView.enableAnimation(true);
			playView.hideInfoText();
			THEME.snd_music_play().loop();
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
				boolean extraLife = game.addPoints(bonus.getValue());
				if (extraLife) {
					THEME.snd_extraLife().play();
				}
				playView.setBonusTimer(app().clock.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			THEME.snd_eatPill().play();
			int points = game.eatFoodAtTile(e.tile);
			boolean extraLife = game.addPoints(points);
			if (extraLife) {
				THEME.snd_extraLife().play();
			}
			if (game.allFoodEaten()) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusReached()) {
				playView.setBonus(game.getBonusSymbol(), game.getBonusValue());
				playView.setBonusTimer(game.getBonusTime());
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
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
			boolean timeForChange = getTicksRemaining() == getDuration() / 2;
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
			ghostAttackTimer.init();
		}
	}

	private class GhostDyingState extends State<GameState, GameEvent> {

		@Override
		public void onEntry() {
			actors.pacMan.setVisible(false);
			boolean extraLife = game.addPoints(game.getKilledGhostValue());
			if (extraLife) {
				THEME.snd_extraLife().play();
			}
			LOGGER.info(String.format("Scored %d points for killing ghost #%d", game.getKilledGhostValue(),
					game.getGhostsKilledByEnergizer()));
		}

		@Override
		public void onTick() {
			actors.getActiveGhosts()
					.filter(ghost -> ghost.getState() == GhostState.DYING || ghost.getState() == GhostState.DEAD)
					.forEach(Ghost::update);
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
			game.saveHiscore();
			THEME.snd_music_play().stop();
			THEME.snd_music_gameover().loop();
		}

		@Override
		public void onExit() {
			playView.hideInfoText();
			THEME.snd_music_gameover().stop();
		}
	}
}