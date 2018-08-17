package de.amr.games.pacman.actor.game;

import java.util.Optional;
import java.util.stream.Stream;

public interface PacManWorld {

	PacMan getPacMan();

	Ghost getBlinky();

	Ghost getPinky();

	Ghost getInky();

	Ghost getClyde();

	Stream<Ghost> getActiveGhosts();

	Optional<Bonus> getBonus();
}