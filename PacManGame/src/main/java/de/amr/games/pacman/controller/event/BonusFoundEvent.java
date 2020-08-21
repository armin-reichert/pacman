package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.api.TemporaryFood;

public class BonusFoundEvent implements PacManGameEvent {

	public final TemporaryFood food;

	public BonusFoundEvent(TemporaryFood food) {
		this.food = food;
	}

	@Override
	public String toString() {
		return String.format("BonusFound(%s)", food);
	}
}