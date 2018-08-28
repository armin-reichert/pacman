package de.amr.games.pacman.actor;

import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.PacManThemes;
import de.amr.games.pacman.view.core.TileAwareView;

public class Bonus extends GameEntityUsingSprites implements TileAwareView {

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
		sprite = PacManThemes.THEME.symbol(symbol);
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
			sprite = PacManThemes.THEME.pinkNumber(index);
		}
	}

	@Override
	public Transform tf() {
		return tf;
	}

	@Override
	public int getTileSize() {
		return Game.TS;
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
	public void draw(Graphics2D g) {
		float dx = tf.getX() - (sprite.getWidth() - getWidth()) / 2;
		float dy = tf.getY() - (sprite.getHeight() - getHeight()) / 2;
		g.translate(dx, dy);
		sprite.draw(g);
		g.translate(-dx, -dy);
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