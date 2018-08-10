package de.amr.statemachine;

import java.util.function.IntSupplier;

/**
 * Implementation of a state in a finite state machine.
 * 
 * @author Armin Reichert
 */
public class StateObject<S, E> {

	/** Constant for defining an unlimited duration. */
	public static final int ENDLESS = Integer.MAX_VALUE;

	/** The label used to identify this state. */
	S id;

	/** The state machine this state belongs to. */
	StateMachine<S, E> machine;

	/** The client code executed when entering this state. */
	Runnable entry;

	/** The client code executed when an update occurs for this state. */
	Runnable update;

	/** The client code executed when leaving this state. */
	Runnable exit;

	/** Function returning state duration. */
	IntSupplier fnDuration;

	/** The number of ticks this state will be active. */
	int timerTotalTicks;

	/** Ticks remaining until time-out */
	int ticksRemaining;

	protected StateObject() {
		fnDuration = () -> ENDLESS;
		ticksRemaining = timerTotalTicks = ENDLESS;
	}

	public S id() {
		return id;
	}

	public void onEntry() {
		if (entry != null) {
			entry.run();
		}
	}

	public void onExit() {
		if (exit != null) {
			exit.run();
		}
	}

	public void onTick() {
		if (update != null) {
			update.run();
		}
	}

	/** Tells if this state has timed out. */
	public boolean isTerminated() {
		return ticksRemaining == 0;
	}

	/** Resets the timer to the complete state duration. */
	public void resetTimer() {
		if (fnDuration == null) {
			throw new IllegalStateException(String.format("Timer function is NULL in state '%s'", id));
		}
		ticksRemaining = timerTotalTicks = fnDuration.getAsInt();
	}

	void timerStep() {
		if (ticksRemaining > 0) {
			--ticksRemaining;
		}
	}

	/**
	 * Returns the duration of this state.
	 * 
	 * @return the state duration (number of updates until this state times out)
	 */
	public int getDuration() {
		return timerTotalTicks;
	}

	/**
	 * Returns the number of updates until this state will time out.
	 * 
	 * @return the number of updates until timeout occurs
	 */
	public int getRemaining() {
		return ticksRemaining;
	}
}