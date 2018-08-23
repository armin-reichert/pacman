package de.amr.games.pacman.actor;

import static java.util.Arrays.binarySearch;

import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.view.theme.PacManTheme;

public class Bonus extends TileWorldEntity {

	private static final int[] POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private final BonusSymbol symbol;
	private final int value;
	private final int index;
	private boolean honored;
	private Sprite sprite;

	public Bonus(BonusSymbol symbol, int value) {
		index = binarySearch(POINTS, value);
		if (index < 0) {
			throw new IllegalArgumentException("Illegal bonus value: " + value);
		}
		this.symbol = symbol;
		this.value = value;
		honored = false;
		sprite = PacManTheme.ASSETS.symbol(symbol);
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
		if (!honored) {
			honored = true;
			sprite = PacManTheme.ASSETS.pinkNumber(index);
		}
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

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}