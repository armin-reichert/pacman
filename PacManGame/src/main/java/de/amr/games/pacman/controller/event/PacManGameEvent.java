package de.amr.games.pacman.controller.event;

public class PacManGameEvent {

	public static boolean isTrivial(PacManGameEvent event) {
		return event instanceof FoodFoundEvent; // TODO
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
