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
 * State machine for controlling the timing of the ghost attacks. Ghosts attack Pac-Man in rounds,
 * changing between chasing and scattering. The duration of these attacks depends on the level and
 * round.
 * 
 * <p>
 * Ghosts also use the current state of this state machine to decide what to do after being
 * frightened or killed.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
class GhostAttackTimer extends StateMachine<GhostState, Void> {

	private int round;
	private boolean suspended;

	public GhostAttackTimer(PacManGame game) {
		super(GhostState.class);
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(() -> game.scatterTicks(round))
				.onEntry(this::logStateEntry)
			.state(CHASING)
				.timeoutAfter(() -> game.chasingTicks(round))
				.onEntry(this::logStateEntry)
				.onExit(() -> ++round)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void init() {
		LOGGER.info(() -> "Initialize ghost attack timer");
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
			LOGGER.info(() -> String.format("%s: suspended %s, remaining time: %d frames (%.2f seconds)",
					getDescription(), getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			LOGGER.info(() -> String.format("%s: resumed %s, remaining time: %d frames (%.2f seconds)",
					getDescription(), getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = false;
		}
	}

	private void logStateEntry() {
		LOGGER.info(() -> String.format("Start %s for %d ticks (%.2f seconds)", getState(), state().getDuration(),
				state().getDuration() / 60f));
	}
}