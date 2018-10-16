package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.statemachine.StateMachine;

/**
 * Controls the timing of the ghost attacks. Ghosts also use the current state of this state machine
 * to decide what to do after being frightened or killed.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class GhostAttackTimer extends StateMachine<GhostState, Void> implements Controller {

	private final PacManGame game;
	private final StateMachine<GameState, GameEvent> master;
	private int round;

	public GhostAttackTimer(PacManGame game, StateMachine<GameState, GameEvent> master) {
		super(GhostState.class);
		this.game = game;
		this.master = master;
		traceTo(LOGGER, app().clock::getFrequency);
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING).timeoutAfter(this::getScatteringDuration).onEntry(this::fireStartScattering)
			.state(CHASING).timeoutAfter(this::getChasingDuration).onEntry(this::fireStartChasing).onExit(this::nextRound)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void init() {
		round = 0;
		super.init();
	}

	private void nextRound() {
		++round;
	}

	private int sec(float seconds) {
		return app().clock.sec(seconds);
	}

	private void fireStartChasing() {
		LOGGER.info("Start chasing, round " + round);
		master.enqueue(new StartChasingEvent());
	}

	private void fireStartScattering() {
		LOGGER.info("Start scattering, round " + round);
		master.enqueue(new StartScatteringEvent());
	}

	private int getScatteringDuration() {
		int level = game.getLevel();
		if (level <= 1) {
			return sec(round <= 1 ? 7 : 5);
		}
		// levels 2-4
		if (level <= 4) {
			return round <= 1 ? sec(7) : round == 2 ? sec(5) : 1;
		}
		// levels 5+
		return round <= 2 ? sec(5) : 1;
	}

	private int getChasingDuration() {
		int level = game.getLevel();
		if (level <= 1) {
			return round <= 2 ? sec(20) : Integer.MAX_VALUE;
		}
		// levels 2-4
		if (level <= 4) {
			return round <= 1 ? sec(20) : round == 2 ? sec(1033) : Integer.MAX_VALUE;
		}
		// levels 5+
		return round <= 1 ? sec(20) : round == 2 ? sec(1037) : Integer.MAX_VALUE;
	}
}