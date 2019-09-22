package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.PacManGame.TS;
import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;

public class Bonus extends SpriteEntity {

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
		tf.setWidth(TS);
		tf.setHeight(TS);
		PacManTheme theme = Application.app().settings.get("theme");
		sprites.set("s_symbol", theme.spr_bonusSymbol(symbol));
		sprites.set("s_number", theme.spr_pinkNumber(index));
		sprites.select("s_symbol");
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
			sprites.select("s_number");
		}
	}

	public Tile getTile() {
		Vector2f center = tf.getCenter();
		return new Tile(Math.round(center.x) / TS, Math.round(center.y) / TS);
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