package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.sound.PacManSoundManager;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;

/**
 * An animation showing Pac-Man chasing the ghosts and scoring points for each killed ghost.
 * 
 * @author Armin Reichert
 */
public class ChaseGhostsAnimationOld extends GameObject {

	private final World world;
	private final ArcadeGameFolks folks;
	private final PacManSoundManager soundManager;
	private final PacMan pacMan;
	private final Sprite ghost;
	private final Sprite[] points = new Sprite[4];
	private Vector2f startPosition;
	private Vector2f endPosition;
	private final boolean[] killed = new boolean[4];
	private int ghostsKilled;

	private Theme theme;
	private IRenderer pacManRenderer;

	public ChaseGhostsAnimationOld(World world, Theme theme, PacManSoundManager soundManager) {
		this.world = world;
		this.theme = theme;
		this.soundManager = soundManager;
		this.folks = new ArcadeGameFolks();
		pacMan = folks.pacMan();
		pacManRenderer = theme.createPacManRenderer(world);
		ghost = ArcadeTheme.ASSETS.makeSprite_ghostFrightened();
		int i = 0;
		for (int number : new int[] { 200, 400, 800, 1600 }) {
			points[i++] = ArcadeTheme.ASSETS.makeSprite_number(number);
		}
		tf.width = (5 * 18);
		tf.height = (18);
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
		pacMan.tf.x = tf.x;
		pacMan.tf.y = tf.y;
		ghostsKilled = 0;
		tf.setPosition(startPosition);
	}

	@Override
	public void start() {
		init();
		tf.vx = .8f;
		soundManager.snd_eatPill().loop();
	}

	@Override
	public void stop() {
		tf.vx = 0;
		soundManager.snd_eatPill().stop();
	}

	@Override
	public boolean isComplete() {
		return tf.x > endPosition.x;
	}

	@Override
	public void update() {
		if (tf.vx > 0) {
			tf.move();
			if (tf.x + tf.width < 0) {
				return;
			}
			pacMan.tf.x += 0.3f;
			if (ghostsKilled < 4) {
				int x = (int) pacMan.tf.x + 4;
				if (x % 16 == 0) {
					pacMan.tf.x += 1;
					killed[ghostsKilled] = true;
					++ghostsKilled;
				}
			}
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g = (Graphics2D) g.create();
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
		g.translate(-tf.x, -tf.y);
		pacManRenderer.render(g);
		g.dispose();
	}
}