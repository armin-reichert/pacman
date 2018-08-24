package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

public class PacManGhostsChasing extends GameEntity {

	private Sprite pacMan;
	private float pacManX;
	private Sprite ghost;
	private Sprite[] points = new Sprite[4];
	private boolean[] killed = new boolean[4];
	private int ghostsKilled;
	private int gap = 17;

	public PacManGhostsChasing() {
		pacMan = PacManTheme.ASSETS.pacManWalking(Top4.E);
		ghost = PacManTheme.ASSETS.ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManTheme.ASSETS.greenNumber(i);
		}
	}

	@Override
	public void init() {
		for (int i = 0; i < 4; i++) {
			killed[i] = false;
		}
		tf.setX(-5 * gap);
		pacManX = 0;
		ghostsKilled = 0;
	}

	@Override
	public void update() {
		if (tf.getVelocityX() > 0) {
			tf.move();
			if (tf.getX() < 0) {
				return;
			}
			pacManX += 0.6f;
			if (ghostsKilled < 4) {
				int x = (int) pacManX + 4;
				if (x % gap == 0) {
					pacManX += 1;
					killed[ghostsKilled] = true;
					++ghostsKilled;
				}
			}
		}
	}

	public void start() {
		init();
		tf.setVelocityX(.8f);
	}

	public void stop() {
		tf.setVelocityX(0);
	}

	public boolean isComplete() {
		return tf.getX() > 224;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < 4; ++i) {
			g.translate(gap * (i + 1), 0);
			if (killed[i]) {
				points[i].draw(g);
			} else {
				ghost.draw(g);
			}
			g.translate(-gap * (i + 1), 0);
		}
		g.translate(pacManX, 0);
		pacMan.draw(g);
		g.translate(-pacManX, 0);
		g.translate(-tf.getX(), -tf.getY());
	}

	@Override
	public int getWidth() {
		return 5 * gap;
	}

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}
}