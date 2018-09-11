package de.amr.games.pacman.actor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface defining what Pac-Man sees from his environment.
 * 
 * @author Armin Reichert
 */
public interface PacManWorld {

	Stream<Ghost> getGhosts();

	Optional<Bonus> getBonus();
}