package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;

public class PacManKilledEvent implements PacManGameEvent {

	public final Ghost killer;

	public PacManKilledEvent(Ghost ghost) {
		this.killer = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManKilledEvent(%s)", killer.name());
	}
}
