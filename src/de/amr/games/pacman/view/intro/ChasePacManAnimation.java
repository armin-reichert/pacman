package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManThemes;

public class ChasePacManAnimation extends GameEntity {

	private final int panelWidth;
	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];
	private int pillTimer;
	private boolean pill;

	public ChasePacManAnimation(int width) {
		this.panelWidth = width;
		pacMan = PacManThemes.THEME.pacManWalking(Top4.W);
		ghosts[0] = PacManThemes.THEME.ghostColored(GhostColor.RED, Top4.W);
		ghosts[1] = PacManThemes.THEME.ghostColored(GhostColor.PINK, Top4.W);
		ghosts[2] = PacManThemes.THEME.ghostColored(GhostColor.TURQUOISE, Top4.W);
		ghosts[3] = PacManThemes.THEME.ghostColored(GhostColor.ORANGE, Top4.W);
		pill = true;
	}

	@Override
	public void init() {
		tf.setX(panelWidth);
		pillTimer = Application.PULSE.secToTicks(0.5f);
	}

	@Override
	public void update() {
		tf.move();
		--pillTimer;
		if (pillTimer == 0) {
			pill = !pill;
			pillTimer = Application.PULSE.secToTicks(0.5f);
		}
	}

	public void start() {
		init();
		tf.setVelocityX(-1.2f);
		PacManThemes.THEME.soundSiren().loop();
	}

	public void stop() {
		tf.setVelocityX(0);
		PacManThemes.THEME.soundSiren().stop();
	}

	public boolean isComplete() {
		return tf.getX() < -getWidth();
	}

	@Override
	public int getWidth() {
		return 88;
	}

	@Override
	public void draw(Graphics2D g) {
		int x = 0;
		g.translate(tf.getX(), tf.getY());
		g.setColor(Color.PINK);
		if (pill) {
			g.fillRect(7, 7, 2, 2);
		} else {
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString("10", 2, 10);
		}
		x = 10;
		g.translate(x, 0);
		pacMan.draw(g);
		g.translate(-x, 0);
		for (int i = 0; i < ghosts.length; ++i) {
			x += 16;
			g.translate(x, 0);
			ghosts[i].draw(g);
			g.translate(-x, 0);
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