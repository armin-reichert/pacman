package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;

public interface Population {

	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	void populate(World world);

	void play(Game game);

	Stream<Creature<?>> creatures();

	Stream<Ghost> ghosts();

	PacMan pacMan();

	Ghost blinky();

	Ghost inky();

	Ghost pinky();

	Ghost clyde();
}