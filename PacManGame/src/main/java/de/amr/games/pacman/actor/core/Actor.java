package de.amr.games.pacman.actor.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.api.FsmContainer;

/**
 * Base class for state-machine controlled actors in the maze.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 */
public abstract class Actor<S> extends Entity implements FsmContainer<S, PacManGameEvent> {

	public final Game game;
	public final String name;

	public Actor(Game game, String name) {
		this.game = game;
		this.name = name;
		tf.width = (Tile.SIZE);
		tf.height = (Tile.SIZE);
	}

	@Override
	public String toString() {
		return String.format("(%s, col:%d, row:%d, %s)", name, tile().col, tile().row, getState());
	}

	public Tile tile() {
		Vector2f center = tf.getCenter();
		return game.maze.tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}
}