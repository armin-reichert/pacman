package de.amr.games.pacman.model.world.arcade;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.BonusFoodState;
import de.amr.games.pacman.model.world.api.Tile;

public class ArcadeBonus implements BonusFood {

	public Tile location;
	public BonusFoodState state;
	public Symbol symbol;
	public int value;
	
	@Override
	public String toString() {
		return String.format("(%s,%s,%s)", symbol, value, state);
	}

	@Override
	public Tile location() {
		return location;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public BonusFoodState state() {
		return state;
	}

	@Override
	public void setState(BonusFoodState state) {
		this.state = state;
	}
}