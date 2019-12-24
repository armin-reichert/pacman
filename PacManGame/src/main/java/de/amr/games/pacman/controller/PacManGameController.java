package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.behavior.Steerings.isFleeingToSafeCornerFrom;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;
import static de.amr.games.pacman.controller.PacManGameState.ABOUT_PLAYING;
import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GETTING_READY;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;
import static de.amr.games.pacman.model.PacManGame.FSM_LOGGER;
import static de.amr.games.pacman.model.Timing.sec;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.actor.core.PacManGameActor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.intro.IntroView;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The Pac-Man game controller (finite state machine).
 * 
 * @author Armin Reichert
 */
public class PacManGameController extends StateMachine<PacManGameState, PacManGameEvent>
		implements VisualController {

	private PacManGame game;
	private PacManTheme theme;
	private PacManGameCast cast;
	private GhostCommand ghostCommand;
	private GhostHouse ghostHouse;
	private View currentView;
	private IntroView introView;
	private PlayView playView;

	public PacManGameController(PacManTheme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		buildStateMachine();
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		traceTo(PacManGame.FSM_LOGGER, () -> 60);
	}

	private void selectView(View view) {
		if (currentView != view) {
			currentView = view;
			currentView.init();
		}
	}

	private void createPlayingEnvironment() {
		game = new PacManGame();
		cast = new PacManGameCast(game, theme);
		cast.actors().forEach(actor -> actor.addEventListener(this::process));
		ghostCommand = new GhostCommand(game);
		ghostHouse = new GhostHouse(cast);
		playView = new PlayView(cast, app().settings.width, app().settings.height);
		playView.fnGhostMotionState = ghostCommand::state;
		playView.ghostHouse = ghostHouse;
		selectView(playView);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.ofNullable(currentView);
	}

	@Override
	public void update() {
		getInput();
		super.update();
		currentView.update();
	}

	private void getInput() {
		handleToggleStateMachineLogging();
		handleToggleGhostFrightenedBehavior();
		handleTogglePacManOverflowBug();
		handleCheats();
	}

	private PlayingState playingState() {
		return state(PLAYING);
	}

	private void buildStateMachine() {
		//@formatter:off
		beginStateMachine()
			
			.description("[GameController]")
			.initialState(INTRO)
			
			.states()
				
				.state(INTRO)
					.onEntry(() -> {
						introView = new IntroView(theme, app().settings.width, app().settings.height);
						selectView(introView);
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						game.init();
						cast.actors().forEach(cast::setActorOnStage);
						ghostHouse.resetGlobalDotCounter();
						ghostHouse.resetGhostDotCounters();
						ghostHouse.disableGlobalDotCounter();
						playView.init();
						playView.message("Ready!");
						theme.snd_clips_all().forEach(Sound::stop);
						theme.snd_ready().play();
					})
					.onTick(() -> {
						cast.actorsOnStage().forEach(PacManGameActor::update);
					})
				
				.state(ABOUT_PLAYING)
					.timeoutAfter(sec(1.7f))
					.onEntry(() -> {
						playView.clearMessage();
						playView.energizerBlinking(true);
						theme.music_playing().volume(.90f);
						theme.music_playing().loop();
					})
					.onTick(() -> {
						cast.actorsOnStage().forEach(PacManGameActor::update);
					})
					.onExit(() -> {
						ghostCommand.init();
					})
				
				.state(PLAYING).customState(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.timeoutAfter(() -> sec(4 + game.level.mazeNumFlashes * PacManTheme.MAZE_FLASH_TIME_MILLIS / 1000))
					.onEntry(() -> {
						theme.snd_clips_all().forEach(Sound::stop);
						cast.pacMan.sprites.select("full");
						ghostHouse.resetGhostDotCounters();
					})
					.onTick(() -> {
						if (state().getTicksConsumed() == sec(2)) {
							cast.ghostsOnStage().forEach(Ghost::hide);
							playView.mazeFlashing(game.level.mazeNumFlashes > 0);
						}
						else if (state().getTicksRemaining() == sec(2)) {
							game.enterLevel(game.level.number + 1);
							cast.actorsOnStage().forEach(MazeResident::init);
							playView.init(); // stops flashing
						} 
						else if (state().getTicksRemaining() < sec(1.8f)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
					.onExit(() -> {
						LOGGER.info(() -> String.format("Ghosts killed in level %d: %d", 
								game.level.number, game.level.ghostKilledInLevel));
					})
				
				.state(GHOST_DYING)
					.timeoutAfter(sec(1))
					.onEntry(() -> {
						cast.pacMan.hide();
					})
					.onTick(() -> {
						cast.bonus().ifPresent(Bonus::update);
						cast.ghostsOnStage()
							.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE))
							.forEach(Ghost::update);
					})
					.onExit(() -> {
						cast.pacMan.show();
					})
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(6) : sec(4))
					.onEntry(() -> {
						theme.snd_clips_all().forEach(Sound::stop);
						game.lives -= app().settings.getAsBoolean("PacMan.immortable") ? 0 : 1;
					})
					.onTick(() -> {
						int passedTime = state().getTicksConsumed();
						// wait first 1.5 sec before starting the "dying" animation
						if (passedTime == sec(1.5f)) {
							cast.ghostsOnStage().forEach(Ghost::hide);
							cast.removeBonus();
							theme.snd_die().play();
							cast.pacMan.sprites.select("dying");
						}
						// run "dying" animation
						if (passedTime > sec(1.5f) && passedTime < sec(2.5f)) {
							cast.pacMan.update();
						}
						if (game.lives == 0) {
							return;
						}
						// if playing continues, init actors and view
						if (passedTime == sec(4)) {
							cast.actorsOnStage().forEach(MazeResident::init);
							theme.music_playing().loop();
							playView.init();
						}
						// let ghosts jump a bit before game play continues
						if (passedTime > sec(4)) {
							cast.ghostsOnStage().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.onEntry(() -> {
						game.saveHiscore();
						cast.ghostsOnStage().forEach(Ghost::show);
						cast.removeBonus();
						theme.music_gameover().play();
						playView.enableAnimations(false);
						playView.message("Game   Over!", Color.RED);
						
					})
					.onExit(() -> {
						theme.music_gameover().stop();
						playView.clearMessage();
					})

			.transitions()
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete())
					.act(this::createPlayingEnvironment)
				
				.when(GETTING_READY).then(ABOUT_PLAYING)
					.onTimeout()
				
				.when(ABOUT_PLAYING).then(PLAYING)
					.onTimeout()
					
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
					.act(() -> ghostCommand.init())
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(() -> ghostCommand.init())
			
				.when(GAME_OVER).then(GETTING_READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					
				.when(GAME_OVER).then(INTRO)
					.condition(() -> !theme.music_gameover().isRunning())
							
		.endStateMachine();
		//@formatter:on
	}

	// Classes implementing the FSM states:

	/**
	 * "Playing" state implementation.
	 */
	public class PlayingState extends State<PacManGameState, PacManGameEvent> {

		@Override
		public void onEntry() {
			cast.ghostsOnStage().forEach(Ghost::show);
			cast.pacMan.setState(PacManState.ALIVE);
			playView.init();
			playView.enableAnimations(true);
			playView.energizerBlinking(true);
		}

		@Override
		public void onTick() {
			ghostCommand.update();
			cast.pacMan.update();
			cast.bonus().ifPresent(Bonus::update);
			for (Iterator<Ghost> it = cast.ghostsOnStage().iterator(); it.hasNext();) {
				Ghost ghost = it.next();
				ghost.nextState = ghostCommand.getState();
				if (ghost.is(LOCKED) && ghostHouse.isReleasing(ghost)) {
					ghost.process(new GhostUnlockedEvent());
				}
				else {
					ghost.update();
				}
			}
		}

		private void onPacManGhostCollision(PacManGameEvent event) {
			PacManGhostCollisionEvent e = (PacManGhostCollisionEvent) event;
			if (e.ghost.is(CHASING, SCATTERING)) {
				enqueue(new PacManKilledEvent(e.ghost));
			}
			else if (e.ghost.is(FRIGHTENED)) {
				enqueue(new GhostKilledEvent(e.ghost));
			}
		}

		private void onPacManKilled(PacManGameEvent event) {
			PacManKilledEvent e = (PacManKilledEvent) event;
			LOGGER.info(() -> String.format("PacMan killed by %s at %s", e.killer.name(), e.killer.tile()));
			theme.music_playing().stop();
			cast.pacMan.process(event);
			playView.energizerBlinking(false);
			ghostHouse.enableGlobalDotCounter();
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			ghostCommand.suspend();
			cast.ghostsOnStage().forEach(ghost -> ghost.process(event));
			cast.pacMan.process(event);
		}

		private void onPacManLostPower(PacManGameEvent event) {
			ghostCommand.resume();
		}

		private void onGhostKilled(PacManGameEvent event) {
			GhostKilledEvent e = (GhostKilledEvent) event;
			LOGGER.info(() -> String.format("Ghost %s killed at %s", e.ghost.name(), e.ghost.tile()));
			theme.snd_eatGhost().play();
			int livesBeforeScoring = game.lives;
			game.scoreKilledGhost(e.ghost.name());
			if (game.lives > livesBeforeScoring) {
				theme.snd_extraLife().play();
			}
			e.ghost.process(event);
		}

		private void onBonusFound(PacManGameEvent event) {
			cast.bonus().ifPresent(bonus -> {
				LOGGER.info(() -> String.format("PacMan found %s and wins %d points", bonus.symbol, bonus.value));
				int livesBeforeScoring = game.lives;
				game.score(bonus.value);
				theme.snd_eatFruit().play();
				if (game.lives > livesBeforeScoring) {
					theme.snd_extraLife().play();
				}
				bonus.process(event);
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			int points = game.eatFoodAt(e.tile);
			int livesBeforeScoring = game.lives;
			game.score(points);
			ghostHouse.updateDotCounters();
			theme.snd_eatPill().play();
			if (game.lives > livesBeforeScoring) {
				theme.snd_extraLife().play();
			}
			if (game.numPelletsRemaining() == 0) {
				enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.isBonusScoreReached()) {
				cast.addBonus();
				cast.bonus().ifPresent(bonus -> {
					LOGGER.info(() -> String.format("Bonus %s added, time: %.2f sec", bonus,
							bonus.state().getDuration() / 60f));
				});
			}
			if (e.energizer) {
				enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	// Input

	private void handleCheats() {
		/* ALT-"K": Kill all available ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			game.level.ghostsKilledByEnergizer = 0;
			cast.ghostsOnStage().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
				game.scoreKilledGhost(ghost.name());
				ghost.process(new GhostKilledEvent(ghost));
			});
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			game.maze.tiles().filter(Tile::containsPellet).forEach(tile -> {
				game.eatFoodAt(tile);
				ghostHouse.updateDotCounters();
			});
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (is(PLAYING)) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.level.number + 1));
				enqueue(new LevelCompletedEvent());
			}
		}
		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			boolean immortable = app().settings.getAsBoolean("PacMan.immortable");
			app().settings.set("PacMan.immortable", !immortable);
			LOGGER.info("Pac-Man immortable = " + app().settings.getAsBoolean("PacMan.immortable"));
		}
	}

	private void handleTogglePacManOverflowBug() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_O)) {
			app().settings.set("PacMan.overflowBug", !app().settings.getAsBoolean("PacMan.overflowBug"));
			LOGGER.info("Overflow bug is " + (app().settings.getAsBoolean("PacMan.overflowBug") ? "on" : "off"));
		}
	}

	private void handleToggleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			FSM_LOGGER.setLevel(FSM_LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + FSM_LOGGER.getLevel());
		}
	}

	private void handleToggleGhostFrightenedBehavior() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			boolean original = app().settings.getAsBoolean("Ghost.fleeRandomly");
			if (original) {
				app().settings.set("Ghost.fleeRandomly", false);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isFleeingToSafeCornerFrom(cast.pacMan)));
				LOGGER.info(() -> "Changed ghost escape behavior to escaping via safe route");
			}
			else {
				app().settings.set("Ghost.fleeRandomly", true);
				cast.ghosts().forEach(ghost -> ghost.during(FRIGHTENED, isMovingRandomlyWithoutTurningBack()));
				LOGGER.info(() -> "Changed ghost escape behavior to original random movement");
			}
		}
	}
}