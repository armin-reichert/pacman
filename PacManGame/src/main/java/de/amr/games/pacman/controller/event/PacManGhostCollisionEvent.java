package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.actor.Ghost;

public class PacManGhostCollisionEvent extends PacManGameEvent {

	public final Ghost ghost;

	public PacManGhostCollisionEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManGhostCollisionEvent(%s)", ghost.name);
	}
}