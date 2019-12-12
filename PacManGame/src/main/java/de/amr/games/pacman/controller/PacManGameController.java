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
import static de.amr.games.pacman.theme.PacManTheme.MAZE_FLASH_TIME_MILLIS;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
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

	public PacManGameController(PacManTheme theme) {
		super(PacManGameState.class);
		this.theme = theme;
		buildStateMachine();
		setIgnoreUnknownEvents(true);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
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
						introView = new IntroView(theme);
						introView.theme.snd_insertCoin().play();
						introView.theme.loadMusic();
						show(introView);
					})
				
				.state(GETTING_READY)
					.timeoutAfter(sec(5))
					.onEntry(() -> {
						game.newGame();
						cast.theme.snd_clips_all().forEach(Sound::stop);
						cast.theme.snd_ready().play();
						cast.actors().forEach(cast::activate);
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
					.timeoutAfter(() -> sec(4 + game.level.mazeNumFlashes * MAZE_FLASH_TIME_MILLIS / 1000))
					.onEntry(() -> {
						cast.theme.snd_clips_all().forEach(Sound::stop);
						cast.pacMan.sprites.select("full");
					})
					.onTick(() -> {
						if (state().getTicksConsumed() == sec(2)) {
							cast.activeGhosts().forEach(Ghost::hide);
							playView.mazeFlashing = game.level.mazeNumFlashes > 0;
						}
						else if (state().getTicksRemaining() == sec(2)) {
							game.nextLevel();
							cast.activeActors().forEach(MazeResident::init);
							cast.ghosts().forEach(ghost -> ghost.foodCount = 0);
							playView.init(); // stops flashing
						} 
						else if (state().getTicksRemaining() < sec(1.8f)) {
							cast.activeGhosts().forEach(Ghost::update);
						}
					})
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(Ghost::getDyingTime)
				
				.state(PACMAN_DYING)
					.timeoutAfter(() -> game.lives > 1 ? sec(6) : sec(4))
					.onEntry(() -> {
						cast.theme.snd_clips_all().forEach(Sound::stop);
						game.lives -= app().settings.getAsBoolean("pacMan.immortable") ? 0 : 1;
					})
					.onTick(() -> {
						int passedTime = state().getTicksConsumed();
						// wait first 1.5 sec before starting the "dying" animation
						if (passedTime == sec(1.5f)) {
							cast.activeGhosts().forEach(Ghost::hide);
							cast.removeBonus();
							cast.theme.snd_die().play();
							cast.pacMan.sprites.select("dying");
						}
						// run "dying" animation
						if (passedTime > sec(1.5f) && passedTime < sec(2)) {
							cast.pacMan.update();
						}
						// inform Pac-Man that he died
						if (passedTime == sec(2)) {
							cast.pacMan.process(new PacManDiedEvent());
						}
						if (game.lives == 0) {
							return;
						}
						// if playing continues, init actors and view
						if (passedTime == sec(4)) {
							cast.activeActors().forEach(MazeResident::init);
							cast.theme.music_playing().loop();
							playView.init();
						}
						// let ghosts jump a bit before game play continues
						if (passedTime > sec(4)) {
							cast.activeGhosts().forEach(Ghost::update);
						}
					})
				
				.state(GAME_OVER)
					.timeoutAfter(min(1))
					.onEntry(() -> {
						cast.activeGhosts().forEach(Ghost::show);
						cast.removeBonus();
						cast.theme.music_gameover().loop();
						playView.enableAnimations(false);
						playView.textColor = Color.RED;
						playView.message = "Game Over!";
						game.finishGame();
					})
					.onExit(() -> {
						cast.theme.music_gameover().stop();
						playView.message = null;
					})

			.transitions()
			
				.when(INTRO).then(GETTING_READY)
					.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
					.act(this::createPlayingStage)
				
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
					.act(() -> ghostMotionTimer.init())
					
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.onTimeout()
					.condition(() -> game.lives == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.onTimeout()
					.condition(() -> game.lives > 0)
					.act(() -> ghostMotionTimer.init())
			
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
				if (ghost.getState() == GhostState.LOCKED && canLeaveHouse(ghost)) {
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
			cast.theme.music_playing().stop();
			cast.pacMan.process(event);
			playView.energizerBlinking.setEnabled(false);
		}

		private void onPacManGainsPower(PacManGameEvent event) {
			ghostMotionTimer.suspend();
			cast.activeGhosts().forEach(ghost -> ghost.process(event));
			cast.pacMan.process(event);
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
				boolean extraLife = game.score(bonus.value);
				cast.theme.snd_eatFruit().play();
				if (extraLife) {
					cast.theme.snd_extraLife().play();
				}
				bonus.process(event);
			});
		}

		private void onFoodFound(PacManGameEvent event) {
			FoodFoundEvent e = (FoodFoundEvent) event;
			int points = game.eatFoodAt(e.tile);
			boolean extraLife = game.score(points);
			updateFoodCounter();
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
			int points = 200 * (int) Math.pow(2, game.level.ghostsKilledByEnergizer);
			boolean extraLife = game.score(points);
			LOGGER.info(() -> String.format("Scored %d points for killing %s ghost", points,
					new String[] { "first", "2nd", "3rd", "4th" }[game.level.ghostsKilledByEnergizer]));
			if (extraLife) {
				cast.theme.snd_extraLife().play();
			}
			game.level.ghostsKilledByEnergizer += 1;
			game.level.ghostKilledInLevel += 1;
			if (game.level.ghostKilledInLevel == 16) {
				game.score(12000);
			}
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

	private void createPlayingStage() {
		game = new PacManGame();
		ghostMotionTimer = new GhostMotionTimer(game);
		cast = new PacManGameCast(game, theme);
		cast.pacMan.addGameEventListener(this::process);
		playView = new PlayView(cast);
		playView.fnGhostAttack = ghostMotionTimer::state;
		show(playView);
	}

	@Override
	public void update() {
		handlePlayingSpeedChange();
		handleToggleStateMachineLogging();
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
			game.maze.tiles().filter(game.maze::containsPellet).forEach(tile -> {
				game.eatFoodAt(tile);
				updateFoodCounter();
			});
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

	private void handleToggleStateMachineLogging() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			Logger log = Logger.getLogger("StateMachineLogger");
			log.setLevel(log.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
			LOGGER.info("State machine logging changed to " + log.getLevel());
		}
	}

	private void handlePlayingSpeedChange() {
		int fps = app().clock.getFrequency();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			setClockFrequency(PacManGame.SPEED_1_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			setClockFrequency(PacManGame.SPEED_2_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			setClockFrequency(PacManGame.SPEED_3_FPS);
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

	// Ghost house rules

	/**
	 * The first control used to evaluate when the ghosts leave home is a personal counter each ghost
	 * retains for tracking the number of dots Pac-Man eats. Each ghost's "dot counter" is reset to zero
	 * when a level begins and can only be active when inside the ghost house, but only one ghost's
	 * counter can be active at any given time regardless of how many ghosts are inside.
	 * 
	 * <p>
	 * The order of preference for choosing which ghost's counter to activate is: Pinky, then Inky, and
	 * then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its dot
	 * counter increased by one. Each ghost also has a "dot limit" associated with his counter, per
	 * level.
	 * 
	 * <p>
	 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its
	 * dot counter is deactivated (but not reset). The most-preferred ghost still waiting inside the
	 * house (if any) activates its timer at this point and begins counting dots.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeaveHouse(Ghost ghost) {
		if (ghost == cast.blinky) {
			return true;
		}
		/*@formatter:off*/
		Ghost nextGhost = Stream.of(cast.pinky, cast.inky, cast.clyde)
				.filter(g -> g.getState() == GhostState.LOCKED)
				.findFirst()
				.orElse(null);
		/*@formatter:on*/

		if (ghost != nextGhost) {
			return false;
		}
		if (ghost.foodCount >= foodLimit(ghost)) {
			return true;
		}
		if (cast.game.globalFoodCounterEnabled && cast.game.globalFoodCount >= getGlobalFoodCounterLimit(ghost)) {
			return true;
		}
		int timeout = cast.game.level.number < 5 ? sec(4) : sec(3);
		if (cast.pacMan.ticksSinceLastMeal > timeout) {
			LOGGER.info(() -> String.format("Releasing ghost %s (Pac-Man eat timer expired)", ghost.name()));
			return true;
		}
		return false;
	}

	private void updateFoodCounter() {
		if (cast.game.globalFoodCounterEnabled) {
			cast.game.globalFoodCount++;
			LOGGER.fine(() -> String.format("Global Food Counter=%d", cast.game.globalFoodCount));
			if (cast.game.globalFoodCount == 32 && cast.clyde.getState() == GhostState.LOCKED) {
				cast.game.globalFoodCounterEnabled = false;
				cast.game.globalFoodCount = 0;
			}
			return;
		}
		/*@formatter:off*/
		Stream.of(cast.pinky, cast.inky, cast.clyde)
			.filter(ghost -> ghost.getState() == GhostState.LOCKED)
			.findFirst()
			.ifPresent(preferredGhost -> {
				preferredGhost.foodCount += 1;
				LOGGER.fine(() -> String.format("Food Counter[%s]=%d", preferredGhost.name(), preferredGhost.foodCount));
		});
		/*@formatter:on*/
	}

	/**
	 * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level
	 * begins. For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This
	 * results in Pinky exiting immediately which, in turn, activates Inky's dot counter. His counter
	 * must then reach or exceed 30 dots before he can leave the house.
	 * 
	 * <p>
	 * Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and starts
	 * counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's
	 * dot limit is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the
	 * house as soon as the level begins from now on.
	 * 
	 * <p>
	 * Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
	 * and will leave the ghost house immediately at the start of every level.
	 * 
	 * @param ghost
	 *                a ghost
	 * @return the ghosts's current food limit
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private int foodLimit(Ghost ghost) {
		if (ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return cast.game.level.number == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return cast.game.level.number == 1 ? 60 : cast.game.level.number == 2 ? 50 : 0;
		}
		return 0;
	}

	private int getGlobalFoodCounterLimit(Ghost ghost) {
		return (ghost == cast.pinky) ? 7 : (ghost == cast.inky) ? 17 : (ghost == cast.clyde) ? 32 : 0;
	}

}