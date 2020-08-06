package de.amr.games.pacman.view.theme.api;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

public interface IRenderer {

	default void resetAnimations() {
	}

	default void enableAnimation(boolean enabled) {
	}

	default void smoothDrawingOn(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	default void smoothDrawingOff(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}