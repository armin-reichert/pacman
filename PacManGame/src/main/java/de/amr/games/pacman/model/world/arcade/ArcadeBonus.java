package de.amr.games.pacman.model.world.arcade;

import de.amr.games.pacman.controller.game.BonusState;
import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * Symbols appearing as bonus food in the Arcade game.
 * 
 * @author Armin Reichert
 */
public enum ArcadeBonus implements BonusFood {

	CHERRIES, STRAWBERRY, PEACH, APPLE, GRAPES, GALAXIAN, BELL, KEY;

	private Tile location;
	private BonusState state;
	private int value;

	@Override
	public Tile location() {
		return location;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public boolean isConsumed() {
		return state == BonusState.CONSUMED;
	}

	@Override
	public void consume() {
		state = BonusState.CONSUMED;
	}

	@Override
	public boolean isPresent() {
		return state == BonusState.PRESENT;
	}

	@Override
	public void show() {
		state = BonusState.PRESENT;
	}

	@Override
	public void hide() {
		state = BonusState.ABSENT;
	}

	public void setLocation(Tile location) {
		this.location = location;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public BonusState state() {
		return state;
	}

	@Override
	public String toString() {
		return String.format("(%s,%s,%s)", name(), value, state);
	}
}