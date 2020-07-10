package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.game.Game;

/**
 * A Pac-Man game population.
 * 
 * @author Armin Reichert
 */
public interface Population {

	/**
	 * @return the world where this population lives.
	 */
	World world();

	/**
	 * Lets the population take part in the given game.
	 * 
	 * @param game the game
	 */
	void takePartIn(Game game);

	/**
	 * @return all members of this population.
	 */
	Stream<Creature<?>> all();

	/**
	 * @return all ghosts in this population
	 */
	Stream<Ghost> ghosts();

	/**
	 * @return the single Pac-Man of this population
	 */
	PacMan pacMan();
}