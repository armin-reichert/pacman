package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.actor.Ghost;

public class PacManKilledEvent extends PacManGameEvent {

	public final Ghost killer;

	public PacManKilledEvent(Ghost ghost) {
		this.killer = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManKilledEvent(%s)", killer.getName());
	}
}
