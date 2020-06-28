package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.map.ArcadeMap;
import de.amr.games.pacman.model.map.CustomMap;

public class Worlds {

	public static final PacManWorld ARCADE = new PacManWorld(new ArcadeMap());

	public static final PacManWorld CUSTOM = new PacManWorld(new CustomMap());
}
