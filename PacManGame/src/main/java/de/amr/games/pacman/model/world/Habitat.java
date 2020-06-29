package de.amr.games.pacman.model.world;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;

/**
 * Where the creatures live.
 * 
 * @author Armin Reichert
 */
public interface Habitat extends FoodContainer {

	Stream<Tile> habitatTiles();

	PacMan pacMan();

	Seat pacManSeat();

	Ghost blinky();

	Ghost inky();

	Ghost pinky();

	Ghost clyde();

	Stream<Ghost> ghosts();

	Stream<Ghost> ghostsOnStage();

	Stream<Creature<?>> creatures();

	Stream<Creature<?>> creaturesOnStage();

	boolean isOnState(Creature<?> creature);

	void putOnStage(Creature<?> creature, boolean onStage);
}