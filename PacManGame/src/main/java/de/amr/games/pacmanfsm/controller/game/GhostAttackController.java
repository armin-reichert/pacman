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
package de.amr.games.pacmanfsm.controller.game;

import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacmanfsm.controller.game.GameController.theGame;
import static de.amr.games.pacmanfsm.controller.game.GhostAttackController.Phase.CHASE;
import static de.amr.games.pacmanfsm.controller.game.GhostAttackController.Phase.PAUSED;
import static de.amr.games.pacmanfsm.controller.game.GhostAttackController.Phase.SCATTER;
import static de.amr.games.pacmanfsm.controller.game.Timing.sec;

import de.amr.games.pacmanfsm.controller.creatures.Folks;
import de.amr.games.pacmanfsm.controller.game.GhostAttackController.Phase;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.MissingTransitionBehavior;
import de.amr.statemachine.core.StateMachine;

/**
 * Controller for the timing of the ghost attack waves. Ghosts change between chasing and scattering mode during each
 * level in 4 rounds of different durations. When a ghost becomes frightened, the timer is stopped and later resumed at
 * this point in time when ghosts are not frightened anymore.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostAttackController extends StateMachine<Phase, String> {

	private static final String EVENT_PAUSE = "Pause";

	public enum Phase {
		SCATTER, CHASE, PAUSED
	}

	private static class Times {
		long scatter;
		long chase;
	}

	private static Times[] createTimes(int n) {
		Times[] times = new Times[n];
		for (int i = 0; i < n; ++i) {
			times[i] = new Times();
		}
		return times;
	}

	private Folks folks;
	private int round;
	private Phase pausedState;

	private static final Times[] LEVEL_1 = createTimes(4);
	private static final Times[] LEVELS_2_4 = createTimes(4);
	private static final Times[] LEVELS_5_ = createTimes(4);

	/*@formatter:off*/
	static {
		LEVEL_1[0].scatter = sec(7);
		LEVEL_1[0].chase   = sec(20);
		LEVEL_1[1].scatter = sec(7);
		LEVEL_1[1].chase   = sec(20);
		LEVEL_1[2].scatter = sec(5);
		LEVEL_1[2].chase   = sec(20);
		LEVEL_1[3].scatter = sec(5);
		LEVEL_1[3].chase   = Long.MAX_VALUE;

		LEVELS_2_4[0].scatter = sec(7);
		LEVELS_2_4[0].chase   = sec(20);
		LEVELS_2_4[1].scatter = sec(7);
		LEVELS_2_4[1].chase   = sec(20);
		LEVELS_2_4[2].scatter = sec(5);
		LEVELS_2_4[2].chase   = sec(1033);
		LEVELS_2_4[3].scatter = 1;
		LEVELS_2_4[3].chase   = Long.MAX_VALUE;

		LEVELS_5_[0].scatter = sec(5);
		LEVELS_5_[0].chase   = sec(20);
		LEVELS_5_[1].scatter = sec(5);
		LEVELS_5_[1].chase   = sec(20);
		LEVELS_5_[2].scatter = sec(5);
		LEVELS_5_[2].chase   = sec(1037);
		LEVELS_5_[3].scatter = 1;
		LEVELS_5_[3].chase   = Long.MAX_VALUE;
	}
	/*@formatter:on*/

	private Times times(int level) {
		return switch (level) {
		case 1 -> LEVEL_1[round];
		case 2, 3, 4 -> LEVELS_2_4[round];
		default -> LEVELS_5_[round];
		};
	}

	public GhostAttackController(Folks folks) {
		super(Phase.class, TransitionMatchStrategy.BY_VALUE);
		this.folks = folks;
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		/*@formatter:off*/
		beginStateMachine()
			.description("Ghost Attack Controller")
			.initialState(SCATTER)
		.states()
			.state(SCATTER)
				.timeoutAfter(() -> times(theGame.level).scatter)
				.onTick(this::notifyGhosts)
				.annotation(() -> "Round " + (round + 1))
			.state(CHASE)
				.timeoutAfter(() -> times(theGame.level).chase)
				.onTick(this::notifyGhosts)
				.annotation(() -> "Round " + (round + 1))
			.state(PAUSED)
		.transitions()
			.when(SCATTER).then(CHASE).onTimeout()
			.when(SCATTER).then(PAUSED).on(EVENT_PAUSE).act(() -> pausedState = getState())
			.when(CHASE).then(SCATTER).onTimeout().act(() -> ++round)
			.when(CHASE).then(PAUSED).on(EVENT_PAUSE).act(() -> pausedState = getState())
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
		process(EVENT_PAUSE);
	}

	public void resumeAttacking() {
		if (getState() != PAUSED) {
			throw new IllegalStateException("GhostCommand: cannot resume attacking from state " + getState());
		}
		resumeState(pausedState); // resumeState() does not reset timer, setState() does!
	}
}