package de.amr.games.pacman.view.theme.api;

public interface IPacManRenderer extends IRenderer {

	default boolean isAnimationStoppedWhenStanding() {
		return true;
	}

	default void stopAnimationWhenStanding(boolean b) {
	}
}
