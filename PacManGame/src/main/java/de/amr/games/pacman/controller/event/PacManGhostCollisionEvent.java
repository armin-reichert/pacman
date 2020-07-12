package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;

public class PacManGhostCollisionEvent implements PacManGameEvent {

	public final Ghost ghost;

	public PacManGhostCollisionEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManGhostCollision(%s, col: %d row: %d)", ghost.name(), ghost.location().col, ghost.location().row);
	}
}
