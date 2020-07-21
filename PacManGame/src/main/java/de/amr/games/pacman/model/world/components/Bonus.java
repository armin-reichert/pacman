package de.amr.games.pacman.model.world.components;

import de.amr.games.pacman.model.world.api.Tile;

public class Bonus {

	public final Tile location;
	public final String symbol;
	public final int value;
	public BonusState state;

	public Bonus(Tile location, String symbol, int value) {
		this.location = location;
		this.symbol = symbol;
		this.value = value;
		this.state = BonusState.INACTIVE;
	}
}