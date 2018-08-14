package de.amr.games.pacman.actor.game;

import java.util.Optional;
import java.util.stream.Stream;

public interface PacManWorld {

	Stream<Ghost> getActiveGhosts();

	Optional<Bonus> getBonus();

}