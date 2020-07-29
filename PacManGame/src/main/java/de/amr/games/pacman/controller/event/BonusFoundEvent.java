package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.arcade.Symbol;

public class BonusFoundEvent implements PacManGameEvent {

	public final Symbol symbol;

	public BonusFoundEvent(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s)", symbol);
	}
}