package de.amr.games.pacman.view.intro;

import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Game.sec;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.sound.PacManSoundManager;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.view.render.sprite.ArcadeSprites;

public class ChasePacManAnimation extends GameObject {

	private final PacManSoundManager soundManager;
	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];
	private int pillTimer;
	private Vector2f startPosition;
	private Vector2f endPosition;
	private boolean pill;

	public ChasePacManAnimation(ArcadeSprites theme, PacManSoundManager soundManager) {
		this.soundManager = soundManager;
		pacMan = theme.spr_pacManWalking(LEFT);
		ghosts[0] = theme.spr_ghostColored(Population.RED_GHOST, LEFT);
		ghosts[1] = theme.spr_ghostColored(Population.PINK_GHOST, LEFT);
		ghosts[2] = theme.spr_ghostColored(Population.CYAN_GHOST, LEFT);
		ghosts[3] = theme.spr_ghostColored(Population.ORANGE_GHOST, LEFT);
		pill = true;
		tf.width = (88);
		tf.height = (16);
	}

	public void setStartPosition(float x, float y) {
		this.startPosition = Vector2f.of(x, y);
	}

	public void setEndPosition(float x, float y) {
		this.endPosition = Vector2f.of(x, y);
	}

	@Override
	public void init() {
		tf.setPosition(startPosition);
		pillTimer = sec(0.5f);
	}

	@Override
	public void update() {
		tf.move();
		if (pillTimer > 0) {
			--pillTimer;
		}
		if (pillTimer == 0) {
			pill = !pill;
			pillTimer = sec(0.5f);
		}
	}

	@Override
	public void start() {
		init();
		tf.vx = -0.8f;
		soundManager.snd_ghost_chase().loop();
	}

	@Override
	public void stop() {
		tf.vx = 0;
		soundManager.snd_ghost_chase().stop();
	}

	@Override
	public boolean isComplete() {
		return tf.x < endPosition.x;
	}

	@Override
	public void draw(Graphics2D g) {
		g = (Graphics2D) g.create();
		int x = 0;
		g.translate(tf.x, tf.y);
		g.setColor(Color.PINK);
		if (pill) {
			g.fillRect(6, 6, 2, 2);
		} else {
			g.setFont(new Font("Arial", Font.BOLD, 8));
			g.drawString("10", 0, 10);
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
		g.translate(-tf.x, -tf.y);
		g.dispose();
	}
}