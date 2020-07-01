package de.amr.games.pacman.model.world.arcade;

public class Bonus {

	public final Symbol symbol;
	public final int value;
	public BonusState state;

	public Bonus(Symbol symbol, int value) {
		this.symbol = symbol;
		this.value = value;
	}
}