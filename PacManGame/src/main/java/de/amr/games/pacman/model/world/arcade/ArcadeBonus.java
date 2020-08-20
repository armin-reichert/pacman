package de.amr.games.pacman.model.world.arcade;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.BonusFoodState;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Symbols appearing as bonus food in the Arcade game.
 * 
 * @author Armin Reichert
 */
public enum ArcadeBonus implements BonusFood {

	CHERRIES, STRAWBERRY, PEACH, APPLE, GRAPES, GALAXIAN, BELL, KEY;

	private Tile location;
	private BonusFoodState state;
	private int value;

	@Override
	public Tile location() {
		return location;
	}

	public void setLocation(Tile location) {
		this.location = location;
	}

	@Override
	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public BonusFoodState state() {
		return state;
	}

	@Override
	public void setState(BonusFoodState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return String.format("(%s,%s,%s)", name(), value, state);
	}
}