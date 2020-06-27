package de.amr.games.pacman.model.map;

import java.util.List;

import de.amr.games.pacman.model.world.GhostHouse;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Seat;
import de.amr.games.pacman.model.world.Tile;

public interface PacManWorldStructure {

	int width();

	int height();

	GhostHouse ghostHouse();

	Seat pacManSeat();

	Tile bonusTile();

	List<Portal> portals();
}
