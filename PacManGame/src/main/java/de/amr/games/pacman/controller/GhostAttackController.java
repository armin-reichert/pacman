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
public class GhostAttackController extends StateMachine<GhostState, Void> {

	/** Ticks for given seconds at 60 Hz */
	private static int sec(int sec) {
		return 60 * sec;
	}

	/** Ticks for given minutes at 60 Hz */
	private static int min(int min) {
		return 3600 * min;
	}

	/*@formatter:off*/
	public static final int[][] SCATTER_TICKS = {
		{ sec(7), sec(7), sec(5), sec(5) }, // Level 1
		{ sec(7), sec(7), sec(5), 1 },      // Level 2-4
		{ sec(5), sec(5), sec(5), 1 },      // Level >= 5
	};
	
	public static final int[][] CHASING_TICKS = {
		{ sec(20), sec(20), sec(20),                Integer.MAX_VALUE }, // Level 1
		{ sec(20), sec(20), min(17) + sec(13) + 14, Integer.MAX_VALUE }, // Level 2-4
		{ sec(20), sec(20), min(17) + sec(17) + 14, Integer.MAX_VALUE }, // Level >= 5
	};
	/*@formatter:on*/

	private int getScatteringDuration() {
		int level = fnLevel.getAsInt();
		return SCATTER_TICKS[(level == 1) ? 0 : (level <= 4) ? 1 : 2][Math.min(round, 3)];
	}

	private int getChasingDuration() {
		int level = fnLevel.getAsInt();
		return SCATTER_TICKS[(level == 1) ? 0 : (level <= 4) ? 1 : 2][Math.min(round, 3)];
	}

	private final IntSupplier fnLevel;
	private int round;
	private boolean suspended;

	public GhostAttackController(IntSupplier fnLevel) {
		super(GhostState.class);
		this.fnLevel = fnLevel;
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING).timeoutAfter(this::getScatteringDuration)
			.state(CHASING).timeoutAfter(this::getChasingDuration).onExit(this::nextRound)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
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
		LOGGER.info("Starting attack round #" + round);
	}
}