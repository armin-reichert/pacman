package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.GameView;
import de.amr.games.pacman.view.core.Pen;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class LoadingView implements GameView {

	private Theme theme;
	private int alpha = -1;
	private int alphaInc;
	private Sprite pacManRight;
	private Sprite ghostsLeft[];
	private int ghostDist[];
	private Direction direction;
	private float x, y;
	private float speed;

	public LoadingView(Theme theme) {
		this.theme = theme;
		pacManRight = theme.spr_pacManWalking(Direction.RIGHT.ordinal());
		ghostsLeft = new Sprite[4];
		for (GhostColor color : GhostColor.values()) {
			ghostsLeft[color.ordinal()] = theme.spr_ghostColored(color, Direction.LEFT.ordinal());
		}
		ghostDist = new int[4];
	}

	@Override
	public boolean visible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	public Theme theme() {
		return theme;
	}

	@Override
	public void onThemeChanged(Theme theme) {
		this.theme = theme;
	}

	@Override
	public void init() {
		direction = Direction.RIGHT;
		x = 0;
		y = 9 * Tile.SIZE;
		speed = 0.9f;
	}

	@Override
	public void update() {
		if (x > 40 * Tile.SIZE) {
			direction = Direction.LEFT;
			y = 27 * Tile.SIZE;
			speed = -1.5f;
			Collections.shuffle(Arrays.asList(ghostsLeft));
			for (int i = 1; i < 4; ++i) {
				ghostDist[i] = ghostDist[i - 1] + (5 + new Random().nextInt(5)) * Tile.SIZE;
			}
		} else if (x < -50 * Tile.SIZE) {
			direction = Direction.RIGHT;
			y = 9 * Tile.SIZE;
			speed = 0.9f;
		}
		x += speed;
	}

	@Override
	public void draw(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			if (alpha > 160) {
				alphaInc = -2;
				alpha = 160;
			} else if (alpha < 0) {
				alphaInc = 2;
				alpha = 0;
			}
			pen.color(new Color(255, 255, 255, alpha));
			pen.fontSize(16);
			pen.hcenter("Loading music...", width(), 18);
			alpha += alphaInc;
		}
		if (direction == Direction.RIGHT) {
			pacManRight.draw(g, x, y);
		} else {
			for (int i = 0; i < 4; ++i) {
				ghostsLeft[i].draw(g, x + ghostDist[i], y);
			}
		}
	}
}