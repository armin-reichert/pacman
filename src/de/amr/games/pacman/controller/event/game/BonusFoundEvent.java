package de.amr.games.pacman.controller.event.game;

import de.amr.games.pacman.model.BonusSymbol;

public class BonusFoundEvent extends GameEvent {

	public final BonusSymbol symbol;
	public final int value;

	public BonusFoundEvent(BonusSymbol symbol, int value) {
		this.symbol = symbol;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s,%d)", symbol, value);
	}
}