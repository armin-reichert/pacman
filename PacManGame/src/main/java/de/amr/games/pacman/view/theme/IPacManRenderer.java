package de.amr.games.pacman.view.theme;

public interface IPacManRenderer extends IRenderer {

	default boolean isAnimationStoppedWhenStanding() {
		return true;
	}

	default void setAnimationStoppedWhenStanding(boolean b) {
	}
}
