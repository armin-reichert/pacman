package de.amr.games.pacman.controller.game;

import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.CHASE;
import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.SCATTER;
import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.PAUSED;
import static de.amr.games.pacman.model.game.Game.sec;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState;
import de.amr.games.pacman.model.game.Game;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Controller for the timing of the ghost attack waves. Ghosts change between chasing and scattering
 * mode during each level in 4 rounds of different durations. When a ghost becomes frightened, the
 * timer is stopped and later resumed at this point in time when ghosts are not frightened anymore.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostCommand extends StateMachine<GhostCommandState, String> {

	public static enum GhostCommandState {
		SCATTER, CHASE, PAUSED
	}

	static class Round {
		long scatterTicks, chaseTicks;

		static Round[] newRounds() {
			Round[] rounds = new Round[5];
			for (int i = 1; i < 5; ++i) {
				rounds[i] = new Round();
			}
			return rounds;
		}
	}

	private static final Round[] L1 = Round.newRounds();
	private static final Round[] L4 = Round.newRounds();
	private static final Round[] L5 = Round.newRounds();

	/*@formatter:off*/
	static {
		// first level
		L1[1].scatterTicks = sec(7);
		L1[1].chaseTicks   = sec(20);
		L1[2].scatterTicks = sec(7);
		L1[2].chaseTicks   = sec(20);
		L1[3].scatterTicks = sec(5);
		L1[3].chaseTicks   = sec(20);
		L1[4].scatterTicks = sec(5);
		L1[4].chaseTicks   = Integer.MAX_VALUE;

		// levels upto 4
		L4[1].scatterTicks = sec(7);
		L4[1].chaseTicks   = sec(20);
		L4[2].scatterTicks = sec(7);
		L4[2].chaseTicks   = sec(20);
		L4[3].scatterTicks = sec(5);
		L4[3].chaseTicks   = sec(1033);
		L4[4].scatterTicks = 1;
		L4[4].chaseTicks   = Integer.MAX_VALUE;

		// levels from 5 on
		L5[1].scatterTicks = sec(5);
		L5[1].chaseTicks   = sec(20);
		L5[2].scatterTicks = sec(5);
		L5[2].chaseTicks   = sec(20);
		L5[3].scatterTicks = sec(5);
		L5[3].chaseTicks   = sec(1037);
		L5[4].scatterTicks = 1;
		L5[4].chaseTicks   = Integer.MAX_VALUE;
	}
	/*@formatter:on*/

	private static long scatterTicks(int level, int round) {
		return level == 1 ? L1[round].scatterTicks : level <= 4 ? L4[round].scatterTicks : L5[round].scatterTicks;
	}

	private static long chaseTicks(int level, int round) {
		return level == 1 ? L1[round].chaseTicks : level <= 4 ? L4[round].chaseTicks : L5[round].chaseTicks;
	}

	private int round; // numbering starts with 1!
	private GhostCommandState suspendedStateId;

	public GhostCommand(Game game, Folks folks) {
		super(GhostCommandState.class, TransitionMatchStrategy.BY_VALUE);
		List<Ghost> ghosts = folks.ghosts().collect(Collectors.toList());
		/*@formatter:off*/
		beginStateMachine()
			.description("Ghost Attack Controller")
			.initialState(SCATTER)
		.states()
			.state(SCATTER)
				.timeoutAfter(() -> scatterTicks(game.level.number, round))
				.onTick(() -> ghosts.forEach(ghost -> ghost.setNextStateToEnter(() -> GhostState.SCATTERING)))
			.state(CHASE)
			.timeoutAfter(() -> chaseTicks(game.level.number, round))
				.onTick(() -> ghosts.forEach(ghost -> ghost.setNextStateToEnter(() -> GhostState.CHASING)))
			.state(PAUSED)
		.transitions()
			.when(SCATTER).then(CHASE).onTimeout()
			.when(SCATTER).then(PAUSED).on("Pause")
			.when(CHASE).then(SCATTER).onTimeout().act(() -> ++round)
			.when(CHASE).then(PAUSED).on("Pause")
		.endStateMachine();
		/*@formatter:on*/
		init();
	}

	@Override
	public void init() {
		round = 1;
		suspendedStateId = null;
		super.init();
	}

	public void stopAttacking() {
		if (getState() != PAUSED) {
			suspendedStateId = getState();
			process("Pause");
		}
	}

	public void resumeAttacking() {
		if (getState() == PAUSED) {
			resumeState(suspendedStateId);
		} else {
			throw new IllegalStateException();
		}
	}
}