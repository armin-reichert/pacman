package de.amr.games.pacman.actor;

import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.entity.Transform;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.PacManThemes;
import de.amr.games.pacman.view.core.TilePlacedEntity;

public class Bonus extends GameEntityUsingSprites implements TilePlacedEntity {

	private static final int[] POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private final BonusSymbol symbol;
	private final int value;
	private final int index;
	private boolean honored;

	public Bonus(BonusSymbol symbol, int value) {
		index = binarySearch(POINTS, value);
		if (index < 0) {
			throw new IllegalArgumentException("Illegal bonus value: " + value);
		}
		this.symbol = symbol;
		this.value = value;
		honored = false;
		tf.setWidth(Game.TS);
		tf.setHeight(Game.TS);
		addSprite("s_symbol", PacManThemes.THEME.symbol(symbol));
		addSprite("s_number", PacManThemes.THEME.pinkNumber(index));
		setCurrentSprite("s_symbol");
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
			setCurrentSprite("s_number");
		}
	}

	@Override
	public Transform getTransform() {
		return tf;
	}

	@Override
	public int getTileSize() {
		return Game.TS;
	}

	@Override
	public void draw(Graphics2D g) {
		float dx = tf.getX() - (getWidth() - tf.getWidth()) / 2;
		float dy = tf.getY() - (getHeight() - tf.getHeight()) / 2;
		g.translate(dx, dy);
		currentSprite().draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}