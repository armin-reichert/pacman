package de.amr.games.pacman.model.world.api;

public class Bonus {

	public final Tile location;
	public final String symbol;
	public final int value;
	public BonusState state;

	public Bonus(Tile location, String symbol, int value, BonusState state) {
		this.location = location;
		this.symbol = symbol;
		this.value = value;
		this.state = state;
	}
}