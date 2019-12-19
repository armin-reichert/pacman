package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.logging.Logger;

import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Timing;
import de.amr.statemachine.StateMachine;

/**
 * Controller for the timing of the ghost motion. Ghosts change between chasing
 * and scattering mode during each level in several rounds. The duration of
 * these rounds depends on the level and round. When a ghost becomes frightened,
 * the timer is stopped and the ghost resumes later in that state.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
class GhostMotionTimer extends StateMachine<GhostState, Void> {

	static final int[][] SCATTERING_TICKS = {
			/*@formatter:off*/
			{ sec(7), sec(7), sec(5), sec(5) }, // Level 1
			{ sec(7), sec(7), sec(5), 1 },      // Level 2-4
			{ sec(5), sec(5), sec(5), 1 },      // Level >= 5
			/*@formatter:on*/
	};

	static final int[][] CHASING_TICKS = {
			/*@formatter:off*/
			{ sec(20), sec(20), sec(20),   Integer.MAX_VALUE }, // Level 1
			{ sec(20), sec(20), sec(1033), Integer.MAX_VALUE }, // Level 2-4
			{ sec(20), sec(20), sec(1037), Integer.MAX_VALUE }, // Level >= 5
			/*@formatter:on*/
	};

	/**
	 * @param levelNumber current game level number
	 * @param round       attack round
	 * @return number of ticks ghost will scatter in this round and level
	 */
	static int scatterTicks(int levelNumber, int round) {
		return SCATTERING_TICKS[(levelNumber == 1) ? 0 : (levelNumber <= 4) ? 1 : 2][Math.min(round, 3)];
	}

	/**
	 * @param levelNumber current game level number
	 * @param round       attack round
	 * @return number of ticks ghost will chase in this round and level
	 */
	static int chasingTicks(int levelNumber, int round) {
		return CHASING_TICKS[(levelNumber == 1) ? 0 : (levelNumber <= 4) ? 1 : 2][Math.min(round, 3)];
	}

	private int round;
	private boolean suspended;

	public GhostMotionTimer(PacManGame game) {
		super(GhostState.class);
		traceTo(Logger.getLogger("StateMachineLogger"), () -> Timing.FPS);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostMotionTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(() -> scatterTicks(game.level.number, round))
			.state(CHASING)
				.timeoutAfter(() -> chasingTicks(game.level.number, round))
				.onExit(() -> ++round)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void init() {
		round = 0;
		suspended = false;
		super.init();
	}

	@Override
	public void update() {
		if (!suspended) {
			super.update();
		}
	}

	public void suspend() {
		if (!suspended) {
			LOGGER.info(() -> String.format("%s: suspended %s, remaining time: %d frames (%.2f seconds)", getDescription(),
					getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			LOGGER.info(() -> String.format("%s: resumed %s, remaining time: %d frames (%.2f seconds)", getDescription(),
					getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = false;
		}
	}
}