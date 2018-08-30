package de.amr.statemachine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A finite state machine.
 *
 * @param <S>
 *          type for identifying states, for example an enumeration type.
 * @param <E>
 *          type of inputs (events).
 * 
 * @author Armin Reichert
 */
public class StateMachine<S, E> {

	public static <SS, EE> StateMachineBuilder<SS, EE> define(Class<SS> stateLabelType, Class<EE> eventType) {
		return new StateMachineBuilder<>(stateLabelType);
	}

	private final Deque<E> eventQ;

	private final Map<S, StateObject<S, E>> stateMap;

	private final Map<S, List<Transition<S, E>>> transitionsFromState;

	private StateMachineTracer<S, E> tracer;

	private String description;

	private S initialState;

	private S currentState;

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelType
	 *                         type for state identifiers
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StateMachine(Class<S> stateLabelType) {
		eventQ = new ArrayDeque<>();
		stateMap = stateLabelType.isEnum() ? new EnumMap(stateLabelType) : new HashMap<>(7);
		transitionsFromState = new HashMap<>(7);
		tracer = new StateMachineTracer<>(this, Logger.getGlobal(), () -> 60);
	}

	/**
	 * Forwards tracing to the given logger.
	 * 
	 * @param log
	 *              a logger
	 */
	public void traceTo(Logger log, IntSupplier fnTicksPerSecond) {
		tracer = new StateMachineTracer<>(this, log, fnTicksPerSecond);
	}

	/**
	 * @return the description of this state machine
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description for this state machine.
	 * 
	 * @param description
	 *                      description text (used by tracing)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the initial state for this state machine.
	 * 
	 * @param initialState
	 *                       initial state
	 */
	public void setInitialState(S initialState) {
		if (initialState == null) {
			throw new IllegalStateException("Initial state cannot be NULL");
		}
		this.initialState = initialState;
	}

	/**
	 * 
	 * @return the initial state of this state machine
	 */
	public S getInitialState() {
		return initialState;
	}

	/**
	 * Adds a state transition.
	 * 
	 * @param from
	 *                    transition source state
	 * @param to
	 *                    transition target state
	 * @param guard
	 *                    condition guarding transition or {@code  null} for always fulfilled
	 * @param action
	 *                    action for transition or {@code null} for no action
	 * @param eventType
	 *                    type of event for transition or {@code null} for no event condition
	 * @param timeout
	 *                    if transition is fired on a timeout
	 */
	public void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, Class<? extends E> eventType,
			boolean timeout) {
		Objects.nonNull(from);
		Objects.nonNull(to);
		if (guard == null) {
			guard = () -> true;
		}
		if (action == null) {
			action = t -> {
			};
		}
		if (timeout && eventType != null) {
			throw new IllegalStateException("Cannot specify timeout and event condition on same transition");
		}
		transitionsFrom(from).add(new Transition<>(this, from, to, guard, action, eventType, timeout));
	}

	/**
	 * Adds an input ("event") to the queue of this state machine.
	 * 
	 * @param event
	 *                some input/event
	 */
	public void enqueue(E event) {
		Objects.nonNull(event);
		eventQ.add(event);
	}

	/**
	 * Processes the given event.
	 * 
	 * @param event
	 *                some input / event
	 */
	public void process(E event) {
		Objects.nonNull(event);
		eventQ.add(event);
		update();
	}

	/**
	 * Tells if the state machine is in any of the given states.
	 * 
	 * @param states
	 *                 non-empty list of state labels
	 * @return <code>true</code> if the state machine is in any of the given states
	 */
	@SuppressWarnings("unchecked")
	public boolean any(S... states) {
		if (states.length == 0) {
			throw new IllegalArgumentException("At least one state ID is needed");
		}
		return Stream.of(states).anyMatch(state -> state.equals(currentState));
	}

	/**
	 * @return the current state (identifier)
	 */
	public S currentState() {
		return currentState;
	}

	/**
	 * Sets state machine directly into given state. Entry action is executed.
	 * 
	 * @param state
	 *                new state
	 */
	public void setState(S state) {
		currentState = state;
		currentStateObject().onEntry();
	}

	/**
	 * 
	 * @return the state object of the current state
	 */
	public <C extends StateObject<S, E>> C currentStateObject() {
		if (currentState == null) {
			throw new IllegalStateException("Cannot access current state object, state machine has not been initialzed");
		}
		return state(currentState);
	}

	/**
	 * Returns the state object with the given identifier. The state object is created on demand.
	 * 
	 * @param state
	 *                a state identifier
	 * @return the state object for the given state identifier
	 */
	@SuppressWarnings("unchecked")
	public <C extends StateObject<S, E>> C state(S state) {
		if (!stateMap.containsKey(state)) {
			return (C) replaceState(state, new StateObject<>());
		}
		return (C) stateMap.get(state);
	}

	/**
	 * Replaces the state object for the given state by the given object.
	 * 
	 * @param state
	 *                      state identifier
	 * @param stateObject
	 *                      state object
	 * @return the new state object
	 */
	public <C extends StateObject<S, E>> C replaceState(S state, C stateObject) {
		stateObject.id = state;
		stateObject.machine = this;
		stateMap.put(state, stateObject);
		return stateObject;
	}

	/**
	 * Tells if the time expired for the current stat is the given percentage of the state's total time.
	 * 
	 * @param pct
	 *              percentage value to check for
	 * 
	 * @return {@code true} if the given percentage of the state's time has been consumed. If the
	 *         current state has no timer returns {@code false}.
	 */
	public boolean stateTimeExpiredPct(int pct) {
		StateObject<S, E> stateObject = currentStateObject();
		if (stateObject.timerTotalTicks == StateObject.ENDLESS) {
			return false;
		}
		float expiredFraction = 1f - (float) stateObject.ticksRemaining / (float) stateObject.timerTotalTicks;
		return 100 * expiredFraction == pct;
	}

	public void resetTimer() {
		currentStateObject().resetTimer();
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing the initial
	 * state's (optional) entry action.
	 */
	public void init() {
		tracer.enteringInitialState(initialState);
		currentState = initialState;
		state(currentState).resetTimer();
		state(currentState).onEntry();
	}

	/**
	 * Triggers an update (processing step) of this state machine.
	 */
	public void update() {
		E event = eventQ.peek();
		if (event == null) {
			// find transition without event
			Optional<Transition<S, E>> match = transitionsFrom(currentState).stream().filter(this::canFire).findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				// perform update for current state
				state(currentState).timerStep();
				state(currentState).onTick();
			}
		} else {
			// find transition for current event
			Optional<Transition<S, E>> match = transitionsFrom(currentState).stream().filter(this::canFire).findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				tracer.unhandledEvent(event);
				throw new IllegalStateException(
						String.format("%s: No transition defined in state '%s' for event '%s'", description, currentState, event));
			}
			eventQ.poll();
		}
	}

	private boolean canFire(Transition<S, E> t) {
		boolean guardOk = t.guard == null || t.guard.getAsBoolean();
		if (t.timeout) {
			return guardOk && state(t.from).isTerminated();
		} else if (t.eventType != null) {
			return guardOk && hasMatchingEvent(t.eventType);
		} else {
			return guardOk;
		}
	}

	// TODO make this configurable
	private boolean hasMatchingEvent(Class<? extends E> eventType) {
		return !eventQ.isEmpty() && eventQ.peek().getClass().equals(eventType);
	}

	private void fireTransition(Transition<S, E> t, E event) {
		t.setEvent(event);
		tracer.firingTransition(t);
		if (currentState == t.to) {
			// keep state: no exit/entry actions are executed
			if (t.action != null) {
				t.action.accept(event);
			}
		} else {
			// change state, execute exit and entry actions
			StateObject<S, E> oldState = state(t.from);
			StateObject<S, E> newState = state(t.to);
			tracer.exitingState(currentState);
			oldState.onExit();
			if (t.action != null) {
				t.action.accept(event);
			}
			currentState = t.to;
			tracer.enteringState(t.to);
			newState.resetTimer();
			newState.onEntry();
		}
	}

	private List<Transition<S, E>> transitionsFrom(S state) {
		if (!transitionsFromState.containsKey(state)) {
			transitionsFromState.put(state, new ArrayList<>(3));
		}
		return transitionsFromState.get(state);
	}
}