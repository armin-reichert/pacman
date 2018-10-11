package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.GhostAttackTimer.GhostAttackState.CHASING;
import static de.amr.games.pacman.controller.GhostAttackTimer.GhostAttackState.SCATTERING;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.GhostAttackTimer.GhostAttackState;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.statemachine.StateMachine;

/**
 * Controls the timing of the ghost attacks. Ghosts also use the current state of this state machine
 * to decide what to do after having been frightened or killed.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostAttackTimer extends StateMachine<GhostAttackState, Void> implements Controller {

	public enum GhostAttackState {
		SCATTERING, CHASING
	}

	private final PacManGameController gameControl;
	private int wave;

	public GhostAttackTimer(PacManGameController gameControl) {
		super(GhostAttackState.class);
		this.gameControl = gameControl;
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING).timeoutAfter(() -> getScatteringTicks())
			.state(CHASING).timeoutAfter(() -> getChasingTicks())
		.transitions()
			.when(SCATTERING).then(CHASING)
				.onTimeout()
				.act(this::startChasing)
			.when(CHASING).then(SCATTERING)
				.onTimeout()
				.act(() -> { wave += 1; startScattering(); })
		.endStateMachine();
		/*@formatter:on*/
		traceTo(LOGGER, app().clock::getFrequency);
	}

	@Override
	public void init() {
		super.init();
		wave = 0;
		startScattering();
	}

	private int sec(float seconds) {
		return app().clock.sec(seconds);
	}

	private void startChasing() {
		LOGGER.info("Start chasing, wave " + wave);
		gameControl.enqueue(new StartChasingEvent());
	}

	private void startScattering() {
		LOGGER.info("Start scattering, wave " + wave);
		gameControl.enqueue(new StartScatteringEvent());
	}

	public GhostState getGhostAttackState() {
		switch (getState()) {
		case SCATTERING:
			return GhostState.SCATTERING;
		case CHASING:
			return GhostState.CHASING;
		default:
			throw new IllegalStateException();
		}
	}

	private int getScatteringTicks() {
		int level = gameControl.getGame().getLevel();
		if (level == 1) {
			return sec(wave <= 1 ? 7 : 5);
		}
		// levels 2-4
		if (2 <= level && level <= 4) {
			return wave <= 1 ? sec(7) : wave == 2 ? sec(5) : 1;
		}
		// levels 5+
		return wave <= 2 ? sec(5) : 1;
	}

	private int getChasingTicks() {
		int level = gameControl.getGame().getLevel();
		if (level == 1) {
			return wave <= 2 ? sec(20) : Integer.MAX_VALUE;
		}
		// levels 2-4
		if (2 <= level && level <= 4) {
			return wave <= 1 ? sec(20) : wave == 2 ? sec(1033) : Integer.MAX_VALUE;
		}
		// levels 5+
		return wave <= 1 ? sec(20) : wave == 2 ? sec(1037) : Integer.MAX_VALUE;
	}
}