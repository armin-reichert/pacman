package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.actor.Ghost;

public class PacManGhostCollisionEvent extends PacManGameEvent {

	public final Ghost ghost;

	public PacManGhostCollisionEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManGhostCollision(%s, col: %d row: %d)", ghost.name, ghost.tile().col, ghost.tile().row);
	}
}
