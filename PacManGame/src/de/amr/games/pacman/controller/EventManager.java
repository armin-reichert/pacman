package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Manages a list of event listeners which are informed about events published by some object.
 * 
 * @param <E>
 *          event type
 */
public class EventManager<E> {

	private final String description;
	private final Set<Consumer<E>> listeners = new LinkedHashSet<>();
	private boolean enabled;

	public EventManager(String description) {
		this.description = description;
		enabled = true;
	}

	public void addListener(Consumer<E> l) {
		listeners.add(l);
	}

	public void removeListener(Consumer<E> l) {
		listeners.remove(l);
	}

	public void publish(E event) {
		if (enabled) {
			LOGGER.info(String.format("%s publishing event '%s'", description, event));
			listeners.forEach(subscriber -> subscriber.accept(event));
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
}