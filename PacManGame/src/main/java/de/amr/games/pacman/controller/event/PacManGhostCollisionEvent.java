package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Tile;

public class PacManGhostCollisionEvent extends PacManGameEvent {

	public final Ghost ghost;
	public final Tile where;

	public PacManGhostCollisionEvent(Ghost ghost, Tile where) {
		this.ghost = ghost;
		this.where = where;
	}

	@Override
	public String toString() {
		return String.format("PacManGhostCollision(with %s at col %d row %d)", ghost.name(), where.col, where.row);
	}
}
