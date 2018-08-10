package de.amr.games.pacman.actor;

import java.util.Optional;
import java.util.stream.Stream;

public interface PacManWorld {

	Stream<Ghost> getActiveGhosts();

	Optional<Bonus> getBonus();

}