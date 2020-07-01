package de.amr.games.pacman.controller.event;

public class BonusFoundEvent implements PacManGameEvent {

	@Override
	public String toString() {
		return String.format("BonusFound");
	}
}