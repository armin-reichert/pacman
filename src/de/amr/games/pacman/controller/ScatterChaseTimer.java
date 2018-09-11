package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.statemachine.StateMachine;

/**
 * Controller for starting the scattering and chase phases.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=3">Gamasutra</a>
 */
public class ScatterChaseTimer extends StateMachine<String, Void> implements Controller {

	private final GameController gameControl;

	public ScatterChaseTimer(GameController gameControl) {
		super(String.class);
		this.gameControl = gameControl;
		/*@formatter:off*/
		define()
			.description("[ScatterChaseTimer]")
			.initialState("init")
		.states()
			.state("init")
			.state("s0").timeoutAfter(() -> getScatterTicks(0))
			.state("c0").timeoutAfter(() -> getChaseTicks(0))
			.state("s1").timeoutAfter(() -> getScatterTicks(1))
			.state("c1").timeoutAfter(() -> getChaseTicks(1))
			.state("s2").timeoutAfter(() -> getScatterTicks(2))
			.state("c2").timeoutAfter(() -> getChaseTicks(2))
			.state("s3").timeoutAfter(() -> getScatterTicks(3))
			.state("c3").timeoutAfter(() -> getChaseTicks(3))
		.transitions()
			.when("init").then("s0").act(this::fireStartScattering)
			.when("s0").then("c0").onTimeout().act(this::fireStartChasing)
			.when("c0").then("s1").onTimeout().act(this::fireStartScattering)
			.when("s1").then("c1").onTimeout().act(this::fireStartChasing)
			.when("c1").then("s2").onTimeout().act(this::fireStartScattering)
			.when("s2").then("c2").onTimeout().act(this::fireStartChasing)
			.when("c2").then("s3").onTimeout().act(this::fireStartScattering)
			.when("s3").then("c3").onTimeout().act(this::fireStartChasing)
		.endStateMachine();
		/*@formatter:on*/
	}

	private int getScatterTicks(int wave) {
		int level = gameControl.getGame().getLevel();
		if (level == 1) {
			return app().clock.sec(wave <= 1 ? 7 : 5);
		}
		// levels 2-4
		if (2 <= level && level <= 4) {
			return wave <= 1 ? app().clock.sec(7) : wave == 2 ? app().clock.sec(5) : 1;
		}
		// levels 5+
		return wave <= 2 ? app().clock.sec(5) : 1;
	}

	private int getChaseTicks(int wave) {
		int level = gameControl.getGame().getLevel();
		if (level == 1) {
			return wave <= 2 ? app().clock.sec(20) : Integer.MAX_VALUE;
		}
		// levels 2-4
		if (2 <= level && level <= 4) {
			return wave <= 1 ? app().clock.sec(20) : wave == 2 ? app().clock.sec(1033) : Integer.MAX_VALUE;
		}
		// levels 5+
		return wave <= 1 ? app().clock.sec(20) : wave == 2 ? app().clock.sec(1037) : Integer.MAX_VALUE;
	}

	private void fireStartChasing() {
		gameControl.enqueue(new StartChasingEvent());
	}

	private void fireStartScattering() {
		gameControl.enqueue(new StartScatteringEvent());
	}
}