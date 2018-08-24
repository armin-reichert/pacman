package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * An animation showing Pac-Man chasing the ghosts and scoring points for each killed ghost.
 * 
 * @author Armin Reichert
 */
public class ChaseGhostsAnimation extends GameEntity {

	private final int panelWidth;
	private final Sprite pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private final boolean[] killed = new boolean[4];

	private float pacManX;
	private int ghostsKilled;

	public ChaseGhostsAnimation(int panelWidth) {
		this.panelWidth = panelWidth;
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
		tf.setX(-getWidth());
		pacManX = 0;
		ghostsKilled = 0;
	}

	public void start() {
		init();
		tf.setVelocityX(.8f);
	}

	public void stop() {
		tf.setVelocityX(0);
	}

	@Override
	public void update() {
		if (tf.getVelocityX() > 0) {
			tf.move();
			if (tf.getX() < 0) {
				return;
			}
			pacManX += 0.5f;
			if (ghostsKilled < 4) {
				int x = (int) pacManX + 4;
				if (x % 16 == 0) {
					pacManX += 1;
					killed[ghostsKilled] = true;
					++ghostsKilled;
				}
			}
		}
	}

	public boolean isComplete() {
		return tf.getX() > panelWidth;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < 4; ++i) {
			g.translate(18 * (i + 1), 0);
			if (killed[i]) {
				points[i].draw(g);
			} else {
				ghost.draw(g);
			}
			g.translate(-18 * (i + 1), 0);
		}
		g.translate(pacManX, 0);
		pacMan.draw(g);
		g.translate(-pacManX, 0);
		g.translate(-tf.getX(), -tf.getY());
	}

	@Override
	public int getWidth() {
		return 5 * 18;
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