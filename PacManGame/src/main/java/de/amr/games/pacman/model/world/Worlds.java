package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.map.ArcadeMap;
import de.amr.games.pacman.model.world.map.CustomMap;

public class Worlds {

	public static PacManWorld arcade() {
		return new PacManWorld(new ArcadeMap());
	}

	public static PacManWorld custom() {
		return new PacManWorld(new CustomMap());
	}
}
