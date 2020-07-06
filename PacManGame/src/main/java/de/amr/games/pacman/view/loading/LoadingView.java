package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.arcade.GhostRenderer;
import de.amr.games.pacman.view.theme.arcade.PacManRenderer;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class LoadingView implements LivingView {

	private final PacMan pacMan;
	private final int width;
	private final int height;
	private int alpha;
	private int alphaInc;
	private int ghostCount;
	private int ghostInc;
	private Random rnd = new Random();

	private IRenderer pacManRenderer;
	private Map<Ghost, IRenderer> ghostRenderer = new HashMap<>();
	private Sprite[][] ghostSprites = new Sprite[4][4];

	public LoadingView(World world, int width, int height) {
		pacMan = world.population().pacMan();
		this.width = width;
		this.height = height;
		pacManRenderer = new PacManRenderer(world);
		world.population().ghosts().forEach(ghost -> ghostRenderer.put(ghost, new GhostRenderer(ghost)));
		for (int color = 0; color < 4; ++color) {
			for (Direction dir : Direction.values()) {
				ghostSprites[color][dir.ordinal()] = ArcadeTheme.ASSETS.makeSprite_ghostColored(color, dir);
			}
		}
	}

	@Override
	public void init() {
		ghostCount = 0;
		ghostInc = 1;
		pacMan.init();
		pacMan.start();
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
			pen.font(Assets.font("font.hud"));
			pen.fontSize(10);
			pen.hcenter(Localized.texts.getString("loading_music"), width, 18, Tile.SIZE);
		}
		pacManRenderer.render(g);
		float x = width / 2 - (ghostCount / 2) * 20, y = pacMan.tf.y + 20;
		for (int i = 0; i < ghostCount; ++i) {
			ghostSprites[rnd.nextInt(4)][rnd.nextInt(4)].draw(g, x, y);
			x += 20;
		}
	}
}