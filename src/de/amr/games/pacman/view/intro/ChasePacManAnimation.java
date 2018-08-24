package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;

public class ChasePacManAnimation extends GameEntity {

	private final int panelWidth;
	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];

	public ChasePacManAnimation(int width) {
		this.panelWidth = width;
		pacMan = PacManTheme.ASSETS.pacManWalking(Top4.W);
		ghosts[0] = PacManTheme.ASSETS.ghostColored(GhostColor.RED, Top4.W);
		ghosts[1] = PacManTheme.ASSETS.ghostColored(GhostColor.PINK, Top4.W);
		ghosts[2] = PacManTheme.ASSETS.ghostColored(GhostColor.TURQUOISE, Top4.W);
		ghosts[3] = PacManTheme.ASSETS.ghostColored(GhostColor.ORANGE, Top4.W);
	}

	@Override
	public void init() {
		tf.setX(panelWidth);
	}

	@Override
	public void update() {
		tf.move();
	}

	public void start() {
		init();
		tf.setVelocityX(-1.5f);
	}

	public void stop() {
		tf.setVelocityX(0);
	}

	public boolean isComplete() {
		return tf.getX() < -getWidth();
	}

	@Override
	public int getWidth() {
		return 80;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		pacMan.draw(g);
		for (int i = 0; i < ghosts.length; ++i) {
			g.translate(16 * (i + 1), 0);
			ghosts[i].draw(g);
			g.translate(-16 * (i + 1), 0);
		}
		g.translate(-tf.getX(), -tf.getY());
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