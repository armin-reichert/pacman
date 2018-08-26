package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.BitSet;
import java.util.Random;
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
	private final Random rnd = new Random();
	private final BitSet killed = new BitSet(4);
	private int timer;

	public GhostPointsAnimation() {
		pacMan = PacManThemes.THEME.pacManWalking(Top4.E);
		ghost = PacManThemes.THEME.ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManThemes.THEME.greenNumber(i);
		}
		timer = -1;
	}

	private void resetTimer() {
		timer = (2 + rnd.nextInt(2)) * Application.PULSE.getFrequency();
	}

	@Override
	public void init() {
		killed.clear();
	}

	public void start() {
		init();
		resetTimer();
	}

	public void stop() {
		timer = -1;
	}

	@Override
	public void update() {
		if (timer > 0) {
			timer -= 1;
			return;
		}
		if (killed.cardinality() == 4) {
			init();
			resetTimer();
			return;
		}
		if (timer == 0) {
			int killNext = rnd.nextInt(4);
			while (killed.get(killNext)) {
				killNext = rnd.nextInt(4);
			}
			killed.set(killNext);
			PacManThemes.THEME.soundEatGhost().play();
			resetTimer();
		}
	}

	public boolean isComplete() {
		return false;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		pacMan.draw(g);
		for (int i = 0; i < 4; ++i) {
			g.translate(18 * (i + 1), 0);
			if (killed.get(i)) {
				points[i].draw(g);
			} else {
				ghost.draw(g);
			}
			g.translate(-18 * (i + 1), 0);
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