package de.amr.games.pacman.view.intro;

import static de.amr.graph.grid.impl.Grid4Topology.E;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.Animation;
import de.amr.games.pacman.theme.Theme;

/**
 * An animation showing Pac-Man chasing the ghosts and scoring points for each killed ghost.
 * 
 * @author Armin Reichert
 */
public class ChaseGhostsAnimation extends Entity implements Animation {

	private final Theme theme;
	private final Sprite pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private Vector2f startPosition;
	private Vector2f endPosition;
	private final boolean[] killed = new boolean[4];
	private float pacManX;
	private int ghostsKilled;

	public ChaseGhostsAnimation(Theme theme) {
		this.theme = theme;
		pacMan = theme.spr_pacManWalking(E);
		ghost = theme.spr_ghostFrightened();
		int i = 0;
		for (int number : new int[] { 200, 400, 800, 1600 }) {
			points[i++] = theme.spr_number(number);
		}
		tf.width =(5 * 18);
		tf.height =(18);
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
		theme.snd_eatPill().loop();
	}

	@Override
	public void stop() {
		tf.setVelocityX(0);
		theme.snd_eatPill().stop();
	}

	@Override
	public boolean isComplete() {
		return tf.x > endPosition.x;
	}

	@Override
	public void update() {
		if (tf.getVelocityX() > 0) {
			tf.move();
			if (tf.x + tf.width < 0) {
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
	public void draw(Graphics2D g) {
		g.translate(tf.x, tf.y);
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
		g.translate(-tf.x, -tf.y);
	}
}