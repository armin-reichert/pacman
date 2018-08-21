package de.amr.statemachine;

import java.util.function.IntSupplier;
import java.util.logging.Logger;

/**
 * A tracer for the state machine operations.
 * 
 * @author Armin Reichert
 *
 * @param   <S>
 *            state identifier type
 * @param E
 *            event type
 */
public class StateMachineTracer<S, E> implements StateMachineListener<S, E> {

	private final StateMachine<S, ?> sm;
	private final Logger log;
	private final IntSupplier fnTicksPerSecond;

	public StateMachineTracer(StateMachine<S, ?> sm, Logger log, IntSupplier fnTicksPerSecond) {
		this.sm = sm;
		this.log = log;
		this.fnTicksPerSecond = fnTicksPerSecond;
	}

	@Override
	public void stateCreated(S state) {
		log.info(String.format("%s created state '%s'", sm.getDescription(), state));
	}

	@Override
	public void unhandledEvent(E event) {
		log.info(String.format("%s in state %s could not handle '%s'", sm.getDescription(),
				sm.currentState(), event));
	}

	@Override
	public void enteringInitialState(S initialState) {
		log.info(String.format("%s entering initial state '%s'", sm.getDescription(), initialState));
	}

	@Override
	public void enteringState(S enteredState) {
		if (sm.state(enteredState).getDuration() != StateObject.ENDLESS) {
			float seconds = sm.state(enteredState).getDuration() / fnTicksPerSecond.getAsInt();
			log.info(String.format("%s entering state '%s' for %.2f seconds (%d frames)",
					sm.getDescription(), enteredState, seconds, sm.state(enteredState).getDuration()));
		} else {
			log.info(String.format("%s entering state '%s'", sm.getDescription(), enteredState));
		}
	}

	@Override
	public void exitingState(S exitedState) {
		log.info(String.format("%s exiting state '%s'", sm.getDescription(), exitedState));
	}

	@Override
	public void firingTransition(Transition<S, E> t) {
		if (t.getEvent() == null) {
			if (t.from != t.to) {
				log.info(String.format("%s changing from '%s' to '%s'", sm.getDescription(), t.from, t.to));
			} else {
				log.info(String.format("%s stays '%s'", sm.getDescription(), t.from));
			}
		} else {
			if (t.from != t.to) {
				log.info(String.format("%s changing from '%s' to '%s' on '%s'", sm.getDescription(), t.from,
						t.to, t.getEvent()));
			} else {
				log.info(String.format("%s stays '%s' on '%s'", sm.getDescription(), t.from, t.getEvent()));
			}
		}
	}
}