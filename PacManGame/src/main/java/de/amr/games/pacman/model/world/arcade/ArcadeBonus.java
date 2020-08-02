package de.amr.games.pacman.model.world.arcade;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.BonusFoodState;
import de.amr.games.pacman.model.world.api.Tile;

public class ArcadeBonus implements BonusFood {

	public final Symbol symbol;
	private Tile location;
	private BonusFoodState state;
	private int value;

	public ArcadeBonus(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return String.format("(%s,%s,%s)", symbol, value, state);
	}

	@Override
	public Tile location() {
		return location;
	}

	public void setLocation(Tile location) {
		this.location = location;
	}

	public void setValue(int value) {
		this.value = value;
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

	public Symbol symbol() {
		return symbol;
	}
}