package de.amr.statemachine;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Representation of a state transition.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 * @param <E>
 *          event type
 */
class Transition<S, E> implements StateTransition<S, E> {

	final StateMachine<S, E> sm;
	final S from;
	final S to;
	final BooleanSupplier guard;
	final Consumer<E> action;
	final Class<? extends E> eventType;
	final boolean timeout;
	private E event;

	public Transition(StateMachine<S, E> sm, S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventType, boolean timeout) {
		this.sm = sm;
		this.from = from;
		this.to = to;
		this.guard = guard;
		this.action = action;
		this.eventType = eventType;
		this.timeout = timeout;
	}

	public E getEvent() {
		return event;
	}

	public void setEvent(E event) {
		Objects.nonNull(event);
		this.event = event;
	}

	@Override
	public StateObject<S, E> from() {
		return sm.state(from);
	}

	@Override
	public StateObject<S, E> to() {
		return sm.state(to);
	}

	@Override
	public Optional<E> event() {
		return Optional.ofNullable(event);
	}
}