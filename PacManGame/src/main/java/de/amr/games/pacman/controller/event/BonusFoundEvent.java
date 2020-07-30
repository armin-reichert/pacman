package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.api.BonusFood;

public class BonusFoundEvent implements PacManGameEvent {

	public final BonusFood food;

	public BonusFoundEvent(BonusFood food) {
		this.food = food;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s)", food);
	}
}