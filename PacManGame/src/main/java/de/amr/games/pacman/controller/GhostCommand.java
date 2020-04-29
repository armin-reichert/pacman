package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Timing.sec;

import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
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
	private static final int[][] TIMES = {
		// round 1            round 2            round 3              round 4
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(  20),   sec(5), Integer.MAX_VALUE },	// Level 1
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(1033),        1, Integer.MAX_VALUE },	// Levels 2-4
		{  sec(5), sec(20),   sec(5), sec(20),   sec(5), sec(1037),        1, Integer.MAX_VALUE },	// Levels 5+
	};
	/*@formatter:on*/

	private final Game game;
	private int round; // starts with 1
	private boolean suspended;

	public GhostCommand(Game game) {
		super(GhostState.class);
		this.game = game;
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostCommand]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(this::scatterDuration)
			.state(CHASING)
				.timeoutAfter(this::chaseDuration)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout().act(() -> ++round)
		.endStateMachine();
		/*@formatter:on*/
		getTracer().setLogger(PacManStateMachineLogging.LOG);
	}

	private int entry(int col) {
		int level = game.level.number;
		int row = level == 1 ? 0 : level <= 4 ? 1 : 2;
		return TIMES[row][col];
	}

	private int scatterDuration() {
		return entry(2 * round - 2);
	}

	private int chaseDuration() {
		return entry(2 * round - 1);
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
			game.ghosts().forEach(ghost -> ghost.followState = getState());
		}
	}

	public void suspend() {
		if (!suspended) {
			loginfo("%s: suspended %s, remaining time: %d frames (%.2f seconds)", getDescription(), getState(),
					state().getTicksRemaining(), state().getTicksRemaining() / 60f);
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			loginfo("%s: resumed %s, remaining time: %d frames (%.2f seconds)", getDescription(), getState(),
					state().getTicksRemaining(), state().getTicksRemaining() / 60f);
			suspended = false;
		}
	}
}