package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.logger;
import static de.amr.games.pacman.controller.GameController.PlayState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameController.PlayState.GAME_OVER;
import static de.amr.games.pacman.controller.GameController.PlayState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameController.PlayState.PLAYING;
import static de.amr.games.pacman.controller.GameController.PlayState.READY;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.function.IntSupplier;
import java.util.logging.Level;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
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
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.BasicGamePanel;
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
	private final PacManGameUI currentView;
	private final StateMachine<PlayState, GameEvent> gameControl;

	public GameController(IntSupplier fnFrequency) {
		maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, fnFrequency);
		actors = new Cast(game);
		currentView = new ExtendedGamePanel(new BasicGamePanel(maze.numCols() * PacManGameUI.TS, (maze.numRows() + 5) * PacManGameUI.TS, game, actors));
		gameControl = createGameControl();
		actors.addObserver(gameControl::process);
	}

	@Override
	public View currentView() {
		return currentView;
	}

	@Override
	public void init() {
		logger.setLevel(Level.INFO);
		actors.getPacMan().getStateMachine().traceTo(logger, game.fnTicksPerSecond);
		actors.getGhosts().map(Ghost::getStateMachine).forEach(sm -> sm.traceTo(logger, game.fnTicksPerSecond));
		actors.setActive(actors.getBlinky(), true);
		actors.setActive(actors.getPinky(), false);
		actors.setActive(actors.getInky(), false);
		actors.setActive(actors.getClyde(), false);
		gameControl.traceTo(logger, game.fnTicksPerSecond);
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
			
				.stay(GHOST_DYING)
					.on(PacManGettingWeakerEvent.class)
				
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.on(PacManDiedEvent.class)
					.condition(() -> game.livesRemaining == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.on(PacManDiedEvent.class)
					.condition(() -> game.livesRemaining > 0)
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
			currentView.enableAnimation(false);
			currentView.showInfo("Ready!", Color.YELLOW);
		}

		@Override
		public void onExit() {
			currentView.enableAnimation(true);
			currentView.hideInfo();
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
			PacMan.State pacManState = actors.getPacMan().getState();
			if (pacManState == PacMan.State.DYING) {
				return;
			}
			if (pacManState == PacMan.State.STEROIDS) {
				Ghost.State ghostState = e.ghost.getState();
				if (ghostState == Ghost.State.AFRAID || ghostState == Ghost.State.AGGRO
						|| ghostState == Ghost.State.SCATTERING) {
					gameControl.enqueue(new GhostKilledEvent(e.ghost));
				}
				return;
			}
			gameControl.enqueue(new PacManKilledEvent(e.ghost));
		}

		private void onPacManKilled(GameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			actors.getPacMan().processEvent(e);
			logger.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
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
			logger.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}

		private void onBonusFound(GameEvent event) {
			actors.getBonus().ifPresent(bonus -> {
				logger.info(() -> String.format("PacMan found bonus %s of value %d", bonus.getSymbol(), bonus.getValue()));
				bonus.setHonored();
				game.score += bonus.getValue();
				currentView.setBonusTimer(game.sec(1));
			});
		}

		private void onFoodFound(GameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			game.maze.setContent(e.tile, Content.EATEN);
			game.foodEaten += 1;
			int oldGameScore = game.score;
			game.score += game.getFoodValue(e.food);
			if (oldGameScore < Game.EXTRALIFE_SCORE && game.score >= Game.EXTRALIFE_SCORE) {
				game.livesRemaining += 1;
			}
			if (game.foodEaten == game.foodTotal) {
				gameControl.enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.foodEaten == Game.FOOD_EATEN_BONUS_1 || game.foodEaten == Game.FOOD_EATEN_BONUS_2) {
				actors.addBonus(game.getBonusSymbol(), game.getBonusValue());
				currentView.setBonusTimer(game.getBonusTime());
			}
			if (e.food == Content.ENERGIZER) {
				game.ghostsKilledInSeries = 0;
				gameControl.enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	private class ChangingLevelState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			currentView.setMazeFlashing(true);
		}

		@Override
		public void onTick() {
			if (getRemaining() == getDuration() / 2) {
				nextLevel();
				currentView.showInfo("Ready!", Color.YELLOW);
				currentView.setMazeFlashing(false);
				currentView.enableAnimation(false);
			} else if (isTerminated()) {
				currentView.hideInfo();
				currentView.enableAnimation(true);
			}
		}

		private void nextLevel() {
			game.level += 1;
			game.foodEaten = 0;
			game.ghostsKilledInSeries = 0;
			game.maze.resetFood();
			actors.init();
		}
	}

	private class GhostDyingState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().visibility = () -> false;
			game.score += game.getGhostValue();
		}

		@Override
		public void onTick() {
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			game.ghostsKilledInSeries += 1;
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
			game.livesRemaining -= 1;
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}
	}

	private class GameOverState extends StateObject<PlayState, GameEvent> {

		@Override
		public void onEntry() {
			currentView.enableAnimation(false);
			currentView.showInfo("Game Over!", Color.RED);
		}

		@Override
		public void onExit() {
			currentView.hideInfo();
		}
	}
}