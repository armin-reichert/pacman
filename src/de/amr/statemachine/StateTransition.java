package de.amr.statemachine;

import java.util.Optional;

/**
 * State transition as seen by client.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          type of state identifiers
 * @param <E>
 *          type of inputs (events)
 */
public interface StateTransition<S, E> {

	/**
	 * The state which is changed by this transition.
	 * 
	 * @return state object
	 */
	public StateObject<S, E> from();

	/**
	 * The state where this transition leads to.
	 * 
	 * @return state object
	 */
	public StateObject<S, E> to();

	/**
	 * The input/event which triggered the execution of this transition.
	 * 
	 * @return optional input which triggered transition
	 */
	public Optional<E> event();

	/**
	 * Convenience method which returns the event that triggered this transition.
	 * 
	 * @return event that triggered this transition cast to the specific event type or {@code null} if
	 *         no event triggered this transition
	 */
	@SuppressWarnings("unchecked")
	public default <T extends E> T typedEvent() {
		Optional<E> event = event();
		return event.isPresent() ? (T) event().get() : null;
	}
}