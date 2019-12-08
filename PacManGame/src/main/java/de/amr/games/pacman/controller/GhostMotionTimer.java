package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.util.logging.Logger;

import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
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

	private int round;
	private boolean suspended;

	public GhostMotionTimer(PacManGame game) {
		super(GhostState.class);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostMotionTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(() -> game.level.scatterTicks(round))
			.state(CHASING)
				.timeoutAfter(() -> game.level.chasingTicks(round))
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