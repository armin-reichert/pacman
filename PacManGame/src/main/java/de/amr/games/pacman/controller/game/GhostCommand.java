package de.amr.games.pacman.controller.game;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.model.game.Game.sec;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.StateMachineRegistry;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.game.Game;
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
public class GhostCommand extends StateMachine<GhostState, Void> {

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
	private boolean suspended;

	public GhostCommand(Game game, Folks folks) {
		super(GhostState.class);
		this.game = game;
		this.ghosts = folks.ghosts().collect(Collectors.toList());
		/*@formatter:off*/
		beginStateMachine()
			.description("GhostCommand")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(this::currentRoundScatterDuration)
			.state(CHASING)
				.timeoutAfter(this::currentRoundChaseDuration)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout().act(this::nextRound)
		.endStateMachine();
		/*@formatter:on*/
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
		suspended = false;
		super.init();
	}

	@Override
	public void update() {
		if (!suspended) {
			super.update();
			ghosts.forEach(ghost -> ghost.nextStateToEnter(this::getState));
		}
	}

	public void suspend() {
		if (!suspended) {
			StateMachineRegistry.IT.loginfo("%s: suspended %s, remaining time: %d frames (%.2f seconds)", getDescription(),
					getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f);
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			StateMachineRegistry.IT.loginfo("%s: resumed %s, remaining time: %d frames (%.2f seconds)", getDescription(),
					getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f);
			suspended = false;
		}
	}
}