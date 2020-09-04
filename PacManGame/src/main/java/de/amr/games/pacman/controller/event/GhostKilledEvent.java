package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;

public class GhostKilledEvent implements PacManGameEvent {

	public final Ghost ghost;

	public GhostKilledEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("GhostKilledEvent(%s)", ghost.name);
	}
}