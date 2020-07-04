package de.amr.games.pacman.view.render.api;

import java.awt.Graphics2D;

public interface IRenderer {

	void draw(Graphics2D g);

	default void resetAnimations() {

	}

	default void enableAnimation(boolean enabled) {

	}

}
