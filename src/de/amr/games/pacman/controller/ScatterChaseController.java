package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.statemachine.StateMachine;

public class ScatterChaseController extends StateMachine<String, Void> implements Controller {
	
	private final GameController gameControl;

	public ScatterChaseController(GameController gameControl) {
		super(String.class);
		this.gameControl = gameControl;
		/*@formatter:off*/
		define()
			.description("[ScatterChaseControl]")
			.initialState("init")
		.states()
			.state("init")
			.state("s0").timeoutAfter(() -> app().clock.sec(7))
			.state("c0").timeoutAfter(() -> app().clock.sec(20))
			.state("s1").timeoutAfter(() -> app().clock.sec(7))
			.state("c1").timeoutAfter(() -> app().clock.sec(20))
			.state("s2").timeoutAfter(() -> app().clock.sec(5))
			.state("c2").timeoutAfter(() -> app().clock.sec(20))
			.state("s3").timeoutAfter(() -> app().clock.sec(5))
			.state("c3").timeoutAfter(() -> app().clock.sec(Integer.MAX_VALUE))
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
	
	private void fireStartChasing() {
		gameControl.process(new StartChasingEvent());
	}
	
	private void fireStartScattering() {
		gameControl.process(new StartScatteringEvent());
	}
}