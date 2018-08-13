package de.amr.games.pacman.actor;

import static de.amr.games.pacman.view.PacManGameUI.SPRITES;

import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;

public class Bonus extends TileWorldEntity {

	private static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private BonusSymbol symbol;
	private int value;
	private boolean honored;
	private Sprite sprite;

	public Bonus(BonusSymbol symbol, int value) {
		this.symbol = symbol;
		this.value = value;
		this.honored = false;
		sprite = SPRITES.symbol(symbol);
	}

	public int getValue() {
		return value;
	}

	public BonusSymbol getSymbol() {
		return symbol;
	}

	public boolean isHonored() {
		return honored;
	}

	public void setHonored() {
		honored = true;
		int index = Arrays.binarySearch(BONUS_POINTS, value);
		if (index < 0) {
			throw new IllegalArgumentException("Illegal bonus value: " + value);
		}
		sprite = SPRITES.pinkNumber(index);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(sprite);
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}