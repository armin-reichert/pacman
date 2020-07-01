package de.amr.games.pacman.controller.event;

public class BonusFoundEvent extends PacManGameEvent {

	@Override
	public String toString() {
		return String.format("BonusFound");
	}
}