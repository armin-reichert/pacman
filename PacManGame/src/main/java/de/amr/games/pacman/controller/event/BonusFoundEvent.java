package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.api.Bonus;

public class BonusFoundEvent implements PacManGameEvent {

	public final Bonus bonus;

	public BonusFoundEvent(Bonus bonus) {
		this.bonus = bonus;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s,%s,%s)", bonus.symbol, bonus.value, bonus.state);
	}
}