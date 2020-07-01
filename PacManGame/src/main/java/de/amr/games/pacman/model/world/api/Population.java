package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;

public interface Population {

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