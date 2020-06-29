package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;

public interface Habitat extends FoodContainer {

	Stream<Tile> habitatTiles();

	PacMan pacMan();

	Ghost blinky();

	Ghost inky();

	Ghost pinky();

	Ghost clyde();

	Stream<Ghost> ghosts();

	Stream<Ghost> ghostsOnStage();

	Stream<Creature<?>> creatures();

	Stream<Creature<?>> creaturesOnStage();

	boolean takesPart(Creature<?> actor);

	void takePart(Creature<?> actor, boolean takesPart);

	/**
	 * @return Pac-Man's seat
	 */
	Seat pacManSeat();

}