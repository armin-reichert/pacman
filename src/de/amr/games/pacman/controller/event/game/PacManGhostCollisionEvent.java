package de.amr.games.pacman.controller.event.game;

import de.amr.games.pacman.actor.Ghost;

public class PacManGhostCollisionEvent extends GameEvent {

	public final Ghost ghost;

	public PacManGhostCollisionEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManGhostCollisionEvent(%s)", ghost.getName());
	}
}