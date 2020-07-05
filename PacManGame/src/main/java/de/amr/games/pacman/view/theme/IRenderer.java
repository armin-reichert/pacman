package de.amr.games.pacman.view.theme;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

public interface IRenderer {

	void draw(Graphics2D g);

	default void resetAnimations() {
	}

	default void enableAnimation(boolean enabled) {
	}
	
	default void smoothOn(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	default void smoothOff(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

}