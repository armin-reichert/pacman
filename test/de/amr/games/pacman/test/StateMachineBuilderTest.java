package de.amr.games.pacman.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

public class StateMachineBuilderTest {

	interface Event {
	}

	class EventX implements Event {

	}

	class StateB extends StateObject<String, Event> {
	}

	@Test
	public void test() {
		/*@formatter:off*/
		StateMachine<String, Event> sm = StateMachine.define(String.class, Event.class)
			.description("SampleFSM")
			.initialState("A")
			.states()
				.state("A")
				.state("B").impl(new StateB())
				.state("C")
			.transitions()
				.when("A").then("B").on(EventX.class).act(t -> {
					System.out.println("Action");
				})
				.when("B").condition(() -> 10 > 9)
			.endStateMachine();
		/*@formatter:on*/

		sm.init();
		assertTrue(sm.currentState().equals("A"));

		sm.enqueue(new EventX());
		sm.update();
		assertTrue(sm.currentState().equals("B"));
	}
}