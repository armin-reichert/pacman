package de.amr.games.pacman.model.world.core;

public class Bonus {

	public final String symbol;
	public final int value;
	public BonusState state;

	public Bonus(String symbol, int value) {
		this.symbol = symbol;
		this.value = value;
	}
}