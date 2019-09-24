package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

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
public class GhostAttackController extends StateMachine<GhostState, Void> {

	private final PacManGame game;
	private int round;
	private boolean suspended;

	public GhostAttackController(PacManGame game) {
		super(GhostState.class);
		this.game = game;
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(this::getScatteringDuration)
			.state(CHASING)
				.timeoutAfter(this::getChasingDuration)
				.onExit(this::nextRound)
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
			LOGGER.info(String.format("%s: suspended in state %s, remaining time: %d frames", getDescription(),
					getState(), getTicksRemaining()));
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			LOGGER.info(String.format("%s: resumed in state %s, remaining time: %d frames", getDescription(),
					getState(), getTicksRemaining()));
			suspended = false;
		}
	}

	private void nextRound() {
		++round;
	}

	private int getScatteringDuration() {
		return game.getGhostScatteringDuration(round);
	}

	private int getChasingDuration() {
		return game.getGhostChasingDuration(round);
	}
}
