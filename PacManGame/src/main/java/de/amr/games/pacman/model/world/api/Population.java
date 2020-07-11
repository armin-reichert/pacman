package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.api.Creature;
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
	Stream<Creature> all();
}