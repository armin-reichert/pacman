package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.BitSet;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManThemes;

/**
 * An animation showing Pac-Man and the four ghosts frightened and showing the points scored for the
 * ghosts.
 * 
 * @author Armin Reichert
 */
public class GhostPointsAnimation extends GameEntity {

	private final Sprite pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private final BitSet killed = new BitSet(4);
	private int killNext = 0;
	private int ghostTimer;
	private int energizerTimer;
	private boolean energizer;

	public GhostPointsAnimation() {
		pacMan = PacManThemes.THEME.pacManWalking(Top4.E);
		ghost = PacManThemes.THEME.ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManThemes.THEME.greenNumber(i);
		}
		ghostTimer = -1;
	}

	private void resetGhostTimer() {
		ghostTimer = Application.PULSE.secToTicks(2);
	}

	@Override
	public void init() {
		killed.clear();
		killNext = 0;
		energizer = true;
	}

	public void start() {
		init();
		resetGhostTimer();
		energizerTimer = Application.PULSE.secToTicks(0.5f);
	}

	public void stop() {
		ghostTimer = -1;
	}

	@Override
	public void update() {
		if (ghostTimer > 0) {
			ghostTimer -= 1;
		}
		if (energizerTimer > 0) {
			energizerTimer -= 1;
		}
		if (energizerTimer == 0) {
			energizer = !energizer;
			energizerTimer = Application.PULSE.secToTicks(0.5f);
		}
		if (ghostTimer == 0) {
			killed.set(killNext);
			killNext = (killNext + 1) % 4;
			PacManThemes.THEME.soundEatGhost().play();
			resetGhostTimer();
			if (killed.cardinality() == 4) {
				init();
			}
		}
	}

	public boolean isComplete() {
		return false;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		int x = 0;
		pacMan.draw(g);
		x += 12;
		g.translate(x, 0);
		if (energizer) {
			g.setColor(Color.PINK);
			g.fillOval(4, 4, 8, 8);
		} else {
			g.setColor(Color.PINK);
			g.setFont(new Font("Arial", Font.BOLD, 8));
			g.drawString("50", 4, 12);
		}
		g.translate(-x, 0);
		for (int i = 0; i < 4; ++i) {
			x += 18;
			g.translate(x, 0);
			if (killed.get(i)) {
				points[i].draw(g);
			} else {
				ghost.draw(g);
			}
			g.translate(-x, 0);
		}
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