package de.amr.games.pacman.actor.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.statemachine.api.FsmContainer;

/**
 * Superclass for all actors.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 */
public abstract class Actor<S> extends Entity implements FsmContainer<S, PacManGameEvent>, MazeResiding {

	public abstract Cast cast();

	public Actor() {
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	public Game game() {
		return cast().game();
	}

	@Override
	public Maze maze() {
		return game().maze();
	}

	public Theme theme() {
		return cast().theme();
	}

	@Override
	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}
}