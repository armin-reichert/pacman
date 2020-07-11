package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.actor.Ghost;

public class GhostKilledEvent implements PacManGameEvent {

	public final Ghost ghost;

	public GhostKilledEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("GhostKilled(%s)", ghost.name());
	}
}