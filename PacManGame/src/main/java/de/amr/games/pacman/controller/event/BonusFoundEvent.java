package de.amr.games.pacman.controller.event;

public class BonusFoundEvent extends PacManGameEvent {

	public final String symbol;
	public final int value;

	public BonusFoundEvent(String symbol, int value) {
		this.symbol = symbol;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s,%d)", symbol, value);
	}
}