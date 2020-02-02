package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
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

	private final Cast cast;
	private final PacMan pacMan;

	private int alpha = -1;
	private int alphaInc;
	private int inc;

	private int count;

	public LoadingView(Theme theme) {
		cast = new Cast(new Game(), theme);
		pacMan = cast.pacMan;
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
		return cast.theme();
	}

	@Override
	public void init() {
		pacMan.init();
		pacMan.setState(PacManState.EATING);
		count = 0;
		inc = 1;
	}

	@Override
	public void update() {
		if (pacMan.tf.getX() > 0.8f * width() || pacMan.tf.getX() < 0.2 * width()) {
			pacMan.setMoveDir(pacMan.moveDir().opposite());
			count += inc;
			if (count == 10 || count == 0) {
				inc = -inc;
			}
		}
		pacMan.tf.setVelocity(Vector2f.smul(2.5f, pacMan.moveDir().vector()));
		pacMan.tf.move();
		pacMan.showWalkingAnimation();
	}

	@Override
	public void draw(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme().fnt_text());
			if (alpha > 160) {
				alphaInc = -2;
				alpha = 160;
			} else if (alpha < 0) {
				alphaInc = 2;
				alpha = 0;
			}
			pen.color(new Color(255, 0, 0, alpha));
			pen.fontSize(14);
			pen.hcenter(PacManApp.texts.getString("loading_music"), width(), 18);
			alpha += alphaInc;
		}
		pacMan.draw(g);
		GhostColor randomColor;
		Direction randomDirection;
		int x = width() / 2 - (count / 2) * 20;
		for (int i = 0; i < count; ++i) {
			randomColor = GhostColor.values()[new Random().nextInt(4)];
			randomDirection = Direction.values()[new Random().nextInt(4)];
			cast.theme().spr_ghostColored(randomColor, randomDirection.ordinal()).draw(g, x, pacMan.tf.getY() + 20);
			x += 20;
		}
	}
}