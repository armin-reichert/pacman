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

	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	/**
	 * Lets the population populate the given world that hopefully accepts them.
	 * 
	 * @param world the world where this population wants to live in
	 */
	void populate(World world);

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