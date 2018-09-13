package de.amr.games.pacman.actor;

import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.entity.Transform;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;

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
		tf.setWidth(getTileSize());
		tf.setHeight(getTileSize());
		setSprite("s_symbol", PacManApp.THEME.spr_bonusSymbol(symbol));
		setSprite("s_number", PacManApp.THEME.spr_pinkNumber(index));
		setSelectedSprite("s_symbol");
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
			setSelectedSprite("s_number");
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
	public void draw(Graphics2D g) {
		float dx = tf.getX() - (getSelectedSprite().getWidth() - tf.getWidth()) / 2;
		float dy = tf.getY() - (getSelectedSprite().getHeight() - tf.getHeight()) / 2;
		g.translate(dx, dy);
		getSelectedSprite().draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}