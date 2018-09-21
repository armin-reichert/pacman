package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;

import de.amr.easy.game.controls.AnimationController;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;

/**
 * An animation showing Pac-Man chasing the ghosts and scoring points for each killed ghost.
 * 
 * @author Armin Reichert
 */
public class ChaseGhostsAnimation extends GameEntity implements View, AnimationController {

	private final Sprite pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private Vector2f startPosition;
	private Vector2f endPosition;
	private final boolean[] killed = new boolean[4];
	private float pacManX;
	private int ghostsKilled;

	public ChaseGhostsAnimation() {
		pacMan = PacManApp.theme.spr_pacManWalking(Top4.E);
		ghost = PacManApp.theme.spr_ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			points[i] = PacManApp.theme.spr_greenNumber(i);
		}
		tf.setWidth(5 * 18);
		tf.setHeight(18);
	}

	public void setStartPosition(float x, float y) {
		this.startPosition = Vector2f.of(x, y);
	}

	public void setEndPosition(float x, float y) {
		this.endPosition = Vector2f.of(x, y);
	}

	@Override
	public void init() {
		for (int i = 0; i < 4; i++) {
			killed[i] = false;
		}
		pacManX = 0;
		ghostsKilled = 0;
		tf.setPosition(startPosition);
	}

	@Override
	public void start() {
		init();
		tf.setVelocityX(.8f);
		PacManApp.theme.snd_waza().loop();
	}

	@Override
	public void stop() {
		tf.setVelocityX(0);
		PacManApp.theme.snd_waza().stop();
	}

	@Override
	public void update() {
		if (tf.getVelocityX() > 0) {
			tf.move();
			if (tf.getX() + tf.getWidth() < 0) {
				return;
			}
			pacManX += 0.3f;
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

	@Override
	public boolean isCompleted() {
		return tf.getX() > endPosition.x;
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
}