package de.amr.games.pacman.view.api;

import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.statemachine.core.StateMachine;

/**
 * All views of the Pac-Man game are themeable and have a lifecycle.
 * 
 * @author Armin Reichert
 */
public interface PacManGameView extends Themeable, View, Lifecycle {

	default Stream<StateMachine<?, ?>> machines() {
		return Stream.empty();
	}
}
