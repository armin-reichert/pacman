package de.amr.games.pacman.view;

import java.beans.PropertyChangeListener;

import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Interface implemented by views of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameView extends View, PropertyChangeListener {

	PacManGame game();

	Maze maze();

	PacManTheme theme();

	PacManGameCast cast();

}
