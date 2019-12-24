package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Timing.sec;

import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.statemachine.core.StateMachine;

/**
 * Controller for the timing of the ghost attack waves. Ghosts change between
 * chasing and scattering mode during each level in several rounds. The duration
 * of these rounds depends on the level and round. When a ghost becomes
 * frightened, the timer is stopped and the ghost resumes later in that state.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostCommand extends StateMachine<GhostState, Void> {

	/*@formatter:off*/
	private final int[][] times = {
		// round 1            round 2            round 3              round 4
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(  20),   sec(5), Integer.MAX_VALUE },	// Level 1
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(1033),        1, Integer.MAX_VALUE },	// Levels 2-4
		{  sec(5), sec(20),   sec(5), sec(20),   sec(5), sec(1037),        1, Integer.MAX_VALUE },	// Levels 5+
	};
	/*@formatter:on*/

	private int row(int levelNumber) {
		return levelNumber == 1 ? 0 : levelNumber < 5 ? 1 : 2;
	}

	private int round; // starts with 1
	private boolean suspended;

	public GhostCommand(PacManGame game) {
		super(GhostState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostCommand]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(() -> times[row(game.level().number)][2 * round - 2])
			.state(CHASING)
				.timeoutAfter(() -> times[row(game.level().number)][2 * round - 1])
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout().act(() -> ++round)
		.endStateMachine();
		/*@formatter:on*/
		traceTo(PacManGame.FSM_LOGGER, () -> 60);
	}

	@Override
	public void init() {
		round = 1;
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