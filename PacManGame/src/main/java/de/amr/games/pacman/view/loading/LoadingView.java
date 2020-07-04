package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.render.arcade.GhostRenderer;
import de.amr.games.pacman.view.render.arcade.PacManRenderer;
import de.amr.games.pacman.view.theme.Theme;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class LoadingView implements LivingView {

	private final PacMan pacMan;
	private final Theme theme;
	private final int width;
	private final int height;
	private int alpha;
	private int alphaInc;
	private int ghostCount;
	private int ghostInc;

	public LoadingView(World world, Theme theme, int width, int height) {
		pacMan = world.population().pacMan();
		this.theme = theme;
		this.width = width;
		this.height = height;
		world.population().pacMan().setRenderer(new PacManRenderer(world.population().pacMan(), theme));
		world.population().ghosts().forEach(ghost -> ghost.setRenderer(new GhostRenderer(ghost, theme)));
	}

	@Override
	public void init() {
		pacMan.init();
		pacMan.start();
		ghostCount = 0;
		ghostInc = 1;
	}

	@Override
	public void update() {
		float x = pacMan.tf.getCenter().x;
		if (x > 0.9f * width || x < 0.1 * width) {
			pacMan.setMoveDir(pacMan.moveDir().opposite());
			ghostCount += ghostInc;
			if (ghostCount == 10 || ghostCount == 0) {
				ghostInc = -ghostInc;
			}
		}
		pacMan.tf.setVelocity(Vector2f.smul(2.5f, pacMan.moveDir().vector()));
		pacMan.tf.move();
		pacMan.getRenderer().showWalking();
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
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		try (Pen pen = new Pen(g)) {
			pen.color(new Color(255, 0, 0, alpha));
			pen.font(theme.fnt_text());
			pen.fontSize(10);
			pen.hcenter(Localized.texts.getString("loading_music"), width, 18, Tile.SIZE);
		}
		pacMan.getRenderer().draw(g);
		float x = width / 2 - (ghostCount / 2) * 20, y = pacMan.tf.y + 20;
		for (int i = 0; i < ghostCount; ++i) {
			int color = new Random().nextInt(4);
			Direction dir = Direction.values()[new Random().nextInt(4)];
			theme.spr_ghostColored(color, dir).draw(g, x, y);
			x += 20;
		}
	}
}