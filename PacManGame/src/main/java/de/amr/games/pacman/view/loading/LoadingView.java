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
	private final Theme theme;
	private final PacMan pacMan;

	private int alpha;
	private int alphaInc;
	private int ghostCount;
	private int ghostInc;

	public LoadingView(Theme theme) {
		this.theme = theme;
		cast = new Cast(new Game());
		cast.dressActors(theme);
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
	public void init() {
		pacMan.init();
		pacMan.setState(PacManState.EATING);
		ghostCount = 0;
		ghostInc = 1;
	}

	@Override
	public void update() {
		float x = pacMan.tf.getCenter().x;
		if (x > 0.9f * width() || x < 0.1 * width()) {
			pacMan.setMoveDir(pacMan.moveDir().opposite());
			ghostCount += ghostInc;
			if (ghostCount == 10 || ghostCount == 0) {
				ghostInc = -ghostInc;
			}
		}
		pacMan.tf.setVelocity(Vector2f.smul(2.5f, pacMan.moveDir().vector()));
		pacMan.tf.move();
		pacMan.showWalkingAnimation();
		alpha += alphaInc;
		if (alpha >= 160) {
			alphaInc = -2;
			alpha = 160;
		} else if (alpha <= 0) {
			alphaInc = 2;
			alpha = 0;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.color(new Color(255, 0, 0, alpha));
			pen.font(theme.fnt_text());
			pen.fontSize(14);
			pen.hcenter(PacManApp.texts.getString("loading_music"), width(), 18);
		}
		pacMan.draw(g);
		float x = width() / 2 - (ghostCount / 2) * 20, y = pacMan.tf.getY() + 20;
		for (int i = 0; i < ghostCount; ++i) {
			GhostColor color = GhostColor.values()[new Random().nextInt(4)];
			Direction dir = Direction.values()[new Random().nextInt(4)];
			theme.spr_ghostColored(color, dir.ordinal()).draw(g, x, y);
			x += 20;
		}
	}
}