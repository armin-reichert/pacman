package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.util.function.IntSupplier;
import java.util.logging.Logger;

import de.amr.games.pacman.actor.GhostState;
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
public class GhostAttackTimer extends StateMachine<GhostState, Void> {

	/** Ticks for given seconds at 60 Hz */
	private static int sec(int sec) {
		return 60 * sec;
	}

	/** Ticks for given minutes at 60 Hz */
	private static int min(int min) {
		return 3600 * min;
	}

	/*@formatter:off*/
	public static final int[][] SCATTERING_DURATION = {
		{ sec(7), sec(7), sec(5), sec(5) }, // Level 1
		{ sec(7), sec(7), sec(5), 1 },      // Level 2-4
		{ sec(5), sec(5), sec(5), 1 },      // Level >= 5
	};
	
	public static final int[][] CHASING_DURATION = {
		{ sec(20), sec(20), sec(20),                Integer.MAX_VALUE }, // Level 1
		{ sec(20), sec(20), min(17) + sec(13) + 14, Integer.MAX_VALUE }, // Level 2-4
		{ sec(20), sec(20), min(17) + sec(17) + 14, Integer.MAX_VALUE }, // Level >= 5
	};
	/*@formatter:on*/

	private int ticks(int[][] table) {
		int level = fnLevel.getAsInt();
		return table[(level == 1) ? 0 : (level <= 4) ? 1 : 2][round < 3 ? round : 3];
	}

	private final IntSupplier fnLevel;
	private int round;
	private boolean suspended;

	public GhostAttackTimer(IntSupplier fnLevel) {
		super(GhostState.class);
		this.fnLevel = fnLevel;
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(() -> ticks(SCATTERING_DURATION))
				.onEntry(this::logStateEntry)
			.state(CHASING)
				.timeoutAfter(() -> ticks(CHASING_DURATION))
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
			LOGGER.info(String.format("%s: suspended in state %s, remaining time: %d frames (%.2f seconds)",
					getDescription(), getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = true;
		}
	}

	public void resume() {
		if (suspended) {
			LOGGER.info(String.format("%s: resumed in state %s, remaining time: %d frames (%.2f seconds)",
					getDescription(), getState(), state().getTicksRemaining(), state().getTicksRemaining() / 60f));
			suspended = false;
		}
	}

	private void logStateEntry() {
		LOGGER.info(() -> String.format("Start %s for %d ticks (%.2f seconds)", getState(), state().getDuration(),
				state().getDuration() / 60f));
	}
}