package de.amr.games.pacman.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger for the finite-state machines used in the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManStateMachineLogging {

	public static final Logger LOGGER = Logger.getLogger(PacManStateMachineLogging.class.getSimpleName());

	static {
		LOGGER.setLevel(Level.OFF);
	}

	public static void setEnabled(boolean enabled) {
		LOGGER.setLevel(enabled ? Level.INFO : Level.OFF);
	}

	public static void toggle() {
		LOGGER.setLevel(LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
	}

	public static void loginfo(String msg, Object... args) {
		LOGGER.info(String.format(msg, args));
	}
}