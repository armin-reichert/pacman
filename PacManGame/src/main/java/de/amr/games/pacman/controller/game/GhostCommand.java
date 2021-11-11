/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.game;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.game.GhostCommand.Phase.CHASE;
import static de.amr.games.pacman.controller.game.GhostCommand.Phase.PAUSED;
import static de.amr.games.pacman.controller.game.GhostCommand.Phase.SCATTER;
import static de.amr.games.pacman.controller.game.Timing.sec;
import static de.amr.games.pacman.model.game.PacManGame.game;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.game.GhostCommand.Phase;
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
public class GhostCommand extends StateMachine<Phase, String> {

	public static enum Phase {
		SCATTER, CHASE, PAUSED
	}

	private static class Times {

		long scatter, chase;
	}

	private static Times[] createTimes() {
		Times[] times = new Times[4];
		for (int i = 0; i < 4; ++i) {
			times[i] = new Times();
		}
		return times;
	}

	private Folks folks;
	private int round;
	private Phase pausedState;

	private final Times[] L1 = createTimes(); // level 1
	private final Times[] L2 = createTimes(); // levels 2-4
	private final Times[] L5 = createTimes(); // levels 5...

	/*@formatter:off*/
	{
		L1[0].scatter = sec(7);
		L1[0].chase   = sec(20);
		L1[1].scatter = sec(7);
		L1[1].chase   = sec(20);
		L1[2].scatter = sec(5);
		L1[2].chase   = sec(20);
		L1[3].scatter = sec(5);
		L1[3].chase   = Long.MAX_VALUE;

		L2[0].scatter = sec(7);
		L2[0].chase   = sec(20);
		L2[1].scatter = sec(7);
		L2[1].chase   = sec(20);
		L2[2].scatter = sec(5);
		L2[2].chase   = sec(1033);
		L2[3].scatter = 1;
		L2[3].chase   = Long.MAX_VALUE;

		L5[0].scatter = sec(5);
		L5[0].chase   = sec(20);
		L5[1].scatter = sec(5);
		L5[1].chase   = sec(20);
		L5[2].scatter = sec(5);
		L5[2].chase   = sec(1037);
		L5[3].scatter = 1;
		L5[3].chase   = Long.MAX_VALUE;
	}
	/*@formatter:on*/

	private Times times(int level) {
		return level >= 5 ? L5[round] : level >= 2 ? L2[round] : L1[round];
	}

	public GhostCommand(Folks folks) {
		super(Phase.class, TransitionMatchStrategy.BY_VALUE);
		this.folks = folks;
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		/*@formatter:off*/
		beginStateMachine()
			.description("Ghost Attack Controller")
			.initialState(SCATTER)
		.states()
			.state(SCATTER)
				.timeoutAfter(() -> times(game.level).scatter)
				.onTick(this::notifyGhosts)
				.annotation(() -> "Round " + (round + 1))
			.state(CHASE)
				.timeoutAfter(() -> times(game.level).chase)
				.onTick(this::notifyGhosts)
				.annotation(() -> "Round " + (round + 1))
			.state(PAUSED)
		.transitions()
			.when(SCATTER).then(CHASE).onTimeout()
			.when(SCATTER).then(PAUSED).on("Pause").act(() -> pausedState = getState())
			.when(CHASE).then(SCATTER).onTimeout().act(() -> ++round)
			.when(CHASE).then(PAUSED).on("Pause").act(() -> pausedState = getState())
		.endStateMachine();
		/*@formatter:on*/
	}

	private void notifyGhosts() {
		folks.ghosts().forEach(ghost -> ghost.nextState = getState() == SCATTER ? SCATTERING : CHASING);
	}

	@Override
	public void init() {
		round = 0;
		pausedState = null;
		super.init();
	}

	public void pauseAttacking() {
		process("Pause");
	}

	public void resumeAttacking() {
		if (getState() != PAUSED) {
			throw new IllegalStateException("GhostCommand: cannot resume attacking from state " + getState());
		}
		resumeState(pausedState); // resumeState() does not reset timer, setState() does!
	}
}