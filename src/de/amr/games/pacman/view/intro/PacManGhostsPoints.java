package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

public class PacManGhostsPoints extends GameEntity {

	private Sprite pacMan;
	private Sprite ghost;
	private Sprite[] points = new Sprite[4];
	private int killed;
	private int timer;

	public PacManGhostsPoints() {
		pacMan = PacManTheme.ASSETS.pacManWalking(Top4.E);
		ghost = PacManTheme.ASSETS.ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManTheme.ASSETS.greenNumber(i);
		}
	}

	@Override
	public void init() {
		killed = -1;
		timer = 60;
	}

	public void start() {
		init();
	}

	public void stop() {
	}

	public boolean isComplete() {
		return false;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < 4; ++i) {
			g.translate(16 * (i + 1), 0);
			if (i <= killed) {
				points[i].draw(g);
			} else {
				ghost.draw(g);
			}
			g.translate(-16 * (i + 1), 0);
		}
		pacMan.draw(g);
		g.translate(-tf.getX(), -tf.getY());
	}

	@Override
	public int getWidth() {
		return 5 * 16;
	}

	@Override
	public void update() {
		timer -= 1;
		if (timer == 0) {
			killed += 1;
			if (killed == 4) {
				killed = -1;
			}
			timer = 60;
		}
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