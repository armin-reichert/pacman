package de.amr.games.pacman.actor.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.api.FsmContainer;

/**
 * Base class for state-machine controlled actors in the maze.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 */
public abstract class Actor<S> extends Entity implements FsmContainer<S, PacManGameEvent> {

	private final Cast cast;
	private final String name;

	public Actor(Cast cast, String name) {
		this.cast = cast;
		this.name = name;
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	@Override
	public String toString() {
		return String.format("(%s, col:%d, row:%d, %s)", name(), tile().col, tile().row, getState());
	}

	public String name() {
		return name;
	}

	public Cast cast() {
		return cast;
	}

	public Game game() {
		return cast().game();
	}

	public Maze maze() {
		return cast().game().maze();
	}

	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}
}