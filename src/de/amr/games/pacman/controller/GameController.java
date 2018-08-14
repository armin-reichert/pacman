package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.controller.GameController.PlayState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameController.PlayState.GAME_OVER;
import static de.amr.games.pacman.controller.GameController.PlayState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PLAYING;
import static de.amr.games.pacman.controller.GameController.PlayState.READY;
import static de.amr.games.pacman.model.Content.ENERGIZER;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.game.Cast;
import de.amr.games.pacman.actor.game.Ghost;
import de.amr.games.pacman.actor.game.GhostState;
import de.amr.games.pacman.actor.game.PacManState;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.ExtendedGamePanel;
import de.amr.games.pacman.view.PacManGameUI;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

public class GameController implements Controller {

	public enum PlayState {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	}

	private final Maze maze;
	private final Game game;
	private final Cast actors;
	private final PacManGameUI gameView;
	private final StateMachine<PlayState, GameEvent> gameControl;

	public GameController() {
		maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		gameView = new ExtendedGamePanel(maze.numCols() * Game.TS, (maze.numRows() + 5) * Game.TS, game, actors);
		gameControl = createGameControl();
		actors.addObserver(gameControl::process);
	}

	@Override
	public View currentView() {
		return gameView;
	}

	@Override
	public void init() {
		LOGGER.setLevel(Level.INFO);
		actors.getPacMan().getStateMachine().traceTo(LOGGER, game.fnTicksPerSecond);
		actors.getGhosts().map(Ghost::getStateMachine).forEach(sm -> sm.traceTo(LOGGER, game.fnTicksPerSecond));
		actors.setActive(actors.getPinky(), false);
		actors.setActive(actors.getInky(), false);
		actors.setActive(actors.getClyde(), false);
		gameControl.traceTo(LOGGER, game.fnTicksPerSecond);
		gameControl.init();
	}

	@Override
	public void update() {
		gameControl.update();
		gameView.update();
	}

	private PlayingState playingState() {
		return gameControl.state(PLAYING);
	}

	private StateMachine<PlayState, GameEvent> createGameControl() {
		return
		//@formatter:off
		StateMachine.define(PlayState.class, GameEvent.class)
			
			.description("[GameControl]")
			.initialState(READY)
			
			.states()
			
				.state(READY)
					.impl(new ReadyState())
					.timeoutAfter(game::getReadyTime)
				
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
					.on(PacManDiedEvent.class)
					.condition(() -> game.lives.get() == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.on(PacManDiedEvent.class)
					.condition(() -> game.lives.get() > 0)
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
			gameView.enableAnimation(false);
			gameView.showInfo("Ready!", Color.YELLOW);
		}

		@Override
		public void onExit() {
			gameView.enableAnimation(true);
			gameView.hideInfo();
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
				if (ghostState == GhostState.AFRAID || ghostState == GhostState.AGGRO
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
				gameView.setBonusTimer(game.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			game.eatFoodAt(e.tile);
			if (game.allFoodEaten()) {
				gameControl.enqueue(new LevelCompletedEvent());
			} else {
				if (e.food == ENERGIZER) {
					gameControl.enqueue(new PacManGainsPowerEvent());
				}
				if (game.isBonusReached()) {
					actors.addBonus(game.getBonusSymbol(), game.getBonusValue());
					gameView.setBonusTimer(game.getBonusTime());
				}
			}
		}
	}

	private class ChangingLevelState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().setFullSprite();
			gameView.setMazeFlashing(true);
		}

		@Override
		public void onTick() {
			boolean timeForChange = getRemaining() == getDuration() / 2;
			if (timeForChange) {
				game.nextLevel();
				actors.init();
				gameView.showInfo("Ready!", Color.YELLOW);
				gameView.setMazeFlashing(false);
				gameView.enableAnimation(false);
			}
		}

		@Override
		public void onExit() {
			gameView.hideInfo();
			gameView.enableAnimation(true);
		}
	}

	private class GhostDyingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().visibility = () -> false;
			game.score.add(game.getKilledGhostValue());
		}

		@Override
		public void onTick() {
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == GhostState.DYING).forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.ghostsKilledInSeries.add(1);
			actors.getPacMan().visibility = () -> true;
		}
	}

	private class PacManDyingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
		}

		@Override
		public void onTick() {
			actors.getPacMan().update();
		}

		@Override
		public void onExit() {
			game.lives.sub(1);
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}
	}

	private class GameOverState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			gameView.enableAnimation(false);
			gameView.showInfo("Game Over!", Color.RED);
			game.score.save(game.getLevel());
		}

		@Override
		public void onExit() {
			gameView.hideInfo();
		}
	}
}