package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.model.world.api.Tile;

public class PacManGhostCollisionEvent implements PacManGameEvent {

	public final Ghost ghost;

	public PacManGhostCollisionEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		Tile location = ghost.entity.tileLocation();
		return String.format("PacManGhostCollision(%s, col: %d row: %d)", ghost.name, location.col, location.row);
	}
}
