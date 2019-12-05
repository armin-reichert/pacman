package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.PacManGame.TS;
import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Bonus symbol that appears at the maze bonus position. When consumed, displays the bonus value for
 * some time, then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Entity {

	static final int[] POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	public final BonusSymbol symbol;
	public final int value;
	public boolean number;

	public Bonus(BonusSymbol symbol, int value, PacManTheme theme) {
		tf.setWidth(TS);
		tf.setHeight(TS);
		this.symbol = symbol;
		this.value = value;
		int index = binarySearch(POINTS, value);
		if (index < 0) {
			throw new IllegalArgumentException("Illegal bonus value: " + value);
		}
		number = false;
		sprites.set("symbol", theme.spr_bonusSymbol(symbol));
		sprites.set("number", theme.spr_pinkNumber(index));
		sprites.select("symbol");
	}

	public void changeIntoNumber() {
		number = true;
		sprites.select("number");
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			float dx = tf.getX() - (sprite.getWidth() - tf.getWidth()) / 2;
			float dy = tf.getY() - (sprite.getHeight() - tf.getHeight()) / 2;
			g.translate(dx, dy);
			sprite.draw(g);
			g.translate(-dx, -dy);
		});
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}