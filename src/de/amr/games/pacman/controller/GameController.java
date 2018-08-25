package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.controller.GameController.PlayState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameController.PlayState.GAME_OVER;
import static de.amr.games.pacman.controller.GameController.PlayState.INTRO;
import static de.amr.games.pacman.controller.GameController.PlayState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PLAYING;
import static de.amr.games.pacman.controller.GameController.PlayState.READY;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import de.amr.easy.game.Application;
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
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.play.ExtendedGamePanel;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * The main controller for the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class GameController implements Controller {

	public enum PlayState {
		INTRO, READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	}

	private final Game game;
	private final Cast actors;
	private final ExtendedGamePanel playView;
	private final IntroView introView;
	private final StateMachine<PlayState, GameEvent> gameControl;
	private ViewController currentView;

	public GameController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		int width = maze.numCols() * Game.TS;
		int height = maze.numRows() * Game.TS;
		introView = new IntroView(width, height);
		playView = new ExtendedGamePanel(width, height, game, actors);
		gameControl = buildStateMachine();
		actors.getPacMan().subscribe(gameControl::process);
	}
	
	private void selectView(ViewController view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	@Override
	public View currentView() {
		return currentView;
	}

	@Override
	public void init() {
		LOGGER.setLevel(Level.INFO);
		actors.getPacMan().traceTo(LOGGER);
		actors.getGhosts().forEach(ghost -> ghost.traceTo(LOGGER));
		gameControl.traceTo(LOGGER, game.fnTicksPerSec);
		gameControl.init();
	}

	@Override
	public void update() {
		gameControl.update();
		currentView.update();
	}

	private PlayingState playingState() {
		return gameControl.state(PLAYING);
	}

	private StateMachine<PlayState, GameEvent> buildStateMachine() {
		return
		//@formatter:off
		StateMachine.define(PlayState.class, GameEvent.class)
			
			.description("[GameControl]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						selectView(introView);
						Assets.sound("sfx/insert-coin.mp3").play();
					})
					.onExit(() -> {
						Assets.sounds().forEach(Sound::stop);
					})
				
				.state(READY)
					.impl(new ReadyState())
					.timeoutAfter(() -> game.sec(4.5f))
				
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
					.act(() -> selectView(playView))
				
				.when(READY).then(PLAYING).onTimeout()
					
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(e -> playingState().onFoodFound(e))
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(e -> playingState().onBonusFound(e))
					
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(e -> playingState().onPacManGhostCollision(e))
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(e -> playingState().onPacManGainsPower(e))
					
				.stay(PLAYING)
					.on(PacManGettingWeakerEvent.class)
					.act(e -> playingState().onPacManGettingWeaker(e))
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(e -> playingState().onPacManLostPower(e))
			
				.when(PLAYING).then(GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(e -> playingState().onGhostKilled(e))
					
				.when(PLAYING).then(PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(e -> playingState().onPacManKilled(e))
					
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
					.condition(() -> actors.getPacMan().getState() == PacManState.DEAD && game.getLives() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.condition(() -> actors.getPacMan().getState() == PacManState.DEAD && game.getLives() > 0)
					.act(() -> actors.init())
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
							
		.endStateMachine();
		//@formatter:on
	}

	private class ReadyState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			game.init();
			actors.init();
			game.removeLife();
			playView.setScoresVisible(true);
			playView.enableAnimation(false);
			playView.showInfo("Ready!", Color.YELLOW);
			Assets.sound("sfx/ready.mp3").play();
		}
		
		@Override
		public void onExit() {
			playView.enableAnimation(true);
			playView.hideInfo();
		}
	}

	private class PlayingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onTick() {
			actors.getPacMan().update();
			actors.getActiveGhosts().forEach(Ghost::update);
		}

		private void onPacManGhostCollision(GameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			PacManState pacManState = actors.getPacMan().getState();
			if (pacManState == PacManState.DYING) {
				return;
			}
			if (pacManState == PacManState.GREEDY) {
				GhostState ghostState = e.ghost.getState();
				if (ghostState == GhostState.FRIGHTENED || ghostState == GhostState.AGGRO
						|| ghostState == GhostState.SCATTERING) {
					gameControl.enqueue(new GhostKilledEvent(e.ghost));
				}
				return;
			}
			gameControl.enqueue(new PacManKilledEvent(e.ghost));
		}

		private void onPacManKilled(GameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			actors.getPacMan().processEvent(e);
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
		}

		private void onPacManGainsPower(GameEvent event) {
			PacManGainsPowerEvent e = (PacManGainsPowerEvent) event;
			actors.getPacMan().processEvent(e);
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
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}

		private void onBonusFound(GameEvent event) {
			actors.getBonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found bonus %s of value %d", bonus.getSymbol(), bonus.getValue()));
				bonus.setHonored();
				game.score.add(bonus.getValue());
				playView.setBonusTimer(game.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			game.eatFoodAtTile(e.tile);
			if (game.allFoodEaten()) {
				gameControl.enqueue(new LevelCompletedEvent());
			} else {
				if (e.energizer) {
					gameControl.enqueue(new PacManGainsPowerEvent());
				}
				if (game.isBonusReached()) {
					actors.addBonus(game.getBonusSymbol(), game.getBonusValue());
					playView.setBonusTimer(game.getBonusTime());
				}
			}
		}
	}

	private class ChangingLevelState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().setFullSprite();
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
			playView.setMazeFlashing(true);
		}

		@Override
		public void onTick() {
			boolean timeForChange = getRemaining() == getDuration() / 2;
			if (timeForChange) {
				game.nextLevel();
				actors.init();
				actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
				playView.showInfo("Ready!", Color.YELLOW);
				playView.setMazeFlashing(false);
				playView.enableAnimation(false);
			}
		}

		@Override
		public void onExit() {
			playView.hideInfo();
			playView.enableAnimation(true);
		}
	}

	private class GhostDyingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().visibility = () -> false;
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
			actors.getPacMan().visibility = () -> true;
		}
	}

	private class PacManDyingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
			Assets.sound("sfx/die.mp3").play();
		}

		@Override
		public void onTick() {
			actors.getPacMan().update();
		}

		@Override
		public void onExit() {
			game.removeLife();
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}
	}

	private class GameOverState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			playView.enableAnimation(false);
			playView.showInfo("Game Over!", Color.RED);
			game.score.save();
		}

		@Override
		public void onExit() {
			playView.hideInfo();
		}
	}
}