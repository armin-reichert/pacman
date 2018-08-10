package de.amr.games.pacman.controller.event.core;

public interface Observer<T> {

	void observe(T publisher);
}