package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * An animation showing Pac-Man and the four ghosts frightened and showing the points scored
 * for the ghosts.
 * 
 * @author Armin Reichert
 */
public class GhostPointsAnimation extends GameEntity {

	private final Sprite pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private int killedIndex;
	private int timer;

	public GhostPointsAnimation() {
		pacMan = PacManTheme.ASSETS.pacManWalking(Top4.E);
		ghost = PacManTheme.ASSETS.ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManTheme.ASSETS.greenNumber(i);
		}
	}
	
	private void resetTimer() {
		timer = 90;
	}

	@Override
	public void init() {
		killedIndex = -1;
	  resetTimer();
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
		pacMan.draw(g);
		for (int i = 0; i < 4; ++i) {
			g.translate(18 * (i + 1), 0);
			if (i <= killedIndex) {
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
	public void update() {
		timer -= 1;
		if (timer <= 0) {
			killedIndex = killedIndex < 3 ? killedIndex + 1 : -1;
			if (killedIndex != -1) {
				Assets.sound("sfx/eat-ghost.mp3").play();
			}
			resetTimer();
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