package de.amr.games.pacman.model.world.api;

public class Bonus {

	public final String symbol;
	public final int value;
	public BonusState state;

	public Bonus(String symbol, int value, BonusState state) {
		this.symbol = symbol;
		this.value = value;
		this.state = state;
	}
}