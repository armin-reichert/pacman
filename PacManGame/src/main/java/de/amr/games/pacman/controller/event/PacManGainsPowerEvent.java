package de.amr.games.pacman.controller.event;

public class PacManGainsPowerEvent implements PacManGameEvent {

	public final int duration;

	public PacManGainsPowerEvent(int ticks) {
		this.duration = ticks;
	}
}
