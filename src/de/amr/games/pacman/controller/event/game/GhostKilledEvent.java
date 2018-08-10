package de.amr.games.pacman.controller.event.game;

import de.amr.games.pacman.actor.Ghost;

public class GhostKilledEvent extends GameEvent {

	public final Ghost ghost;

	public GhostKilledEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("GhostKilled(%s)", ghost.getName());
	}
}