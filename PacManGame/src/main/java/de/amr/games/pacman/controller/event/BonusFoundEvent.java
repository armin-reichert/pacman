package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.Symbol;

public class BonusFoundEvent extends PacManGameEvent {

	public final Symbol symbol;
	public final int value;

	public BonusFoundEvent(Symbol symbol, int value) {
		this.symbol = symbol;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s,%d)", symbol, value);
	}
}