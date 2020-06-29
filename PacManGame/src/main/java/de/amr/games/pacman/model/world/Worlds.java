package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.map.ArcadeMap;
import de.amr.games.pacman.model.world.map.CustomMap;

public class Worlds {

	public static PacManWorldImpl arcade() {
		return new PacManWorldImpl(new ArcadeMap());
	}

	public static FoodContainer custom() {
		return new PacManWorldImpl(new CustomMap());
	}
}
