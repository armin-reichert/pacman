package de.amr.games.pacman.controller.game;

import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandEvent.SUSPEND;
import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.CHASE;
import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.SCATTER;
import static de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState.SUSPENDED;
import static de.amr.games.pacman.model.game.Game.sec;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.StateMachineRegistry;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.game.GhostCommand.GhostCommandEvent;
import de.amr.games.pacman.controller.game.GhostCommand.GhostCommandState;
import de.amr.games.pacman.model.game.Game;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Controller for the timing of the ghost attack waves. Ghosts change between chasing and scattering
 * mode during each level in several rounds of different durations. When a ghost becomes frightened,
 * the timer is stopped and the ghost resumes later at this point in time of the round.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostCommand extends StateMachine<GhostCommandState, GhostCommandEvent> {

	public static enum GhostCommandState {
		SCATTER, CHASE, SUSPENDED
	}

	public static enum GhostCommandEvent {
		SUSPEND
	}

	/*@formatter:off*/
	private static final int[][] DURATION_TABLE = {
//     round 1            round 2            round 3              round 4
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(  20),   sec(5), Integer.MAX_VALUE },	// Level 1
		{  sec(7), sec(20),   sec(7), sec(20),   sec(5), sec(1033),        1, Integer.MAX_VALUE },	// Level 2-4
		{  sec(5), sec(20),   sec(5), sec(20),   sec(5), sec(1037),        1, Integer.MAX_VALUE },	// Level >=5
	};
	/*@formatter:on*/

	private final Game game;
	private final List<Ghost> ghosts;
	private int round; // starts with 1
	private GhostCommandState suspendedStateId;

	public GhostCommand(Game game, Folks folks) {
		super(GhostCommandState.class, TransitionMatchStrategy.BY_VALUE);
		this.game = game;
		this.ghosts = folks.ghosts().collect(Collectors.toList());
		/*@formatter:off*/
		beginStateMachine()
			.description("GhostCommand")
			.initialState(SCATTER)
		.states()
			.state(SCATTER)
				.timeoutAfter(this::currentRoundScatterDuration)
				.annotation(() -> String.format("%d of %d ticks", state().getTicksConsumed(), state().getDuration()))
				.onTick(() -> ghosts.forEach(ghost -> ghost.setNextStateToEnter(() -> GhostState.SCATTERING)))
			.state(CHASE)
				.timeoutAfter(this::currentRoundChaseDuration)
				.annotation(() -> String.format("%d of %d ticks", state().getTicksConsumed(), state().getDuration()))
				.onTick(() -> ghosts.forEach(ghost -> ghost.setNextStateToEnter(() -> GhostState.CHASING)))
			.state(SUSPENDED)
		.transitions()
			.when(SCATTER).then(CHASE).onTimeout()
			.when(SCATTER).then(SUSPENDED).on(SUSPEND)
			.when(CHASE).then(SCATTER).onTimeout().act(this::nextRound)
			.when(CHASE).then(SUSPENDED).on(SUSPEND)
		.endStateMachine();
		/*@formatter:on*/
	}

	public void stopAttacks() {
		if (getState() == SUSPENDED) {
			return;
		}
		suspendedStateId = getState();
		process(SUSPEND);
		StateMachineRegistry.IT.loginfo("%s: suspended %s, remaining: %d ticks (%.2f seconds)", getDescription(),
				suspendedStateId, state(suspendedStateId).getTicksRemaining(),
				state(suspendedStateId).getTicksRemaining() / 60f);
	}

	public void resumeAttacks() {
		if (getState() != SUSPENDED) {
			throw new IllegalStateException();
		}
		resumeState(suspendedStateId);
		StateMachineRegistry.IT.loginfo("%s: resumed %s, time: %d frames (%.2f seconds)", getDescription(), getState(),
				state().getTicksRemaining(), state().getTicksRemaining() / 60f);
	}

	private int tableEntry(int col) {
		int level = game.level.number;
		int row = level == 1 ? 0 : level <= 4 ? 1 : 2;
		return DURATION_TABLE[row][col];
	}

	private int currentRoundScatterDuration() {
		return tableEntry(2 * round - 2);
	}

	private int currentRoundChaseDuration() {
		return tableEntry(2 * round - 1);
	}

	private void nextRound() {
		++round;
	}

	@Override
	public void init() {
		round = 1;
		suspendedStateId = null;
		super.init();
	}
}