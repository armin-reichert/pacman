package de.amr.games.pacman.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PacManStateMachineLogging {

	public static final Logger LOG = Logger.getLogger("StateMachineLogger");

	static {
		LOG.setLevel(Level.OFF);
	}

	public static void toggle() {
		LOG.setLevel(LOG.getLevel() == Level.INFO ? Level.OFF : Level.INFO);
	}
}