package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.controls.AnimationController;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.GhostColor;

public class ChasePacManAnimation extends GameEntity implements AnimationController, View {

	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];
	private int pillTimer;
	private Vector2f startPosition;
	private Vector2f endPosition;
	private boolean pill;

	public ChasePacManAnimation() {
		pacMan = PacManApp.THEME.spr_pacManWalking(Top4.W);
		ghosts[0] = PacManApp.THEME.spr_ghostColored(GhostColor.RED, Top4.W);
		ghosts[1] = PacManApp.THEME.spr_ghostColored(GhostColor.PINK, Top4.W);
		ghosts[2] = PacManApp.THEME.spr_ghostColored(GhostColor.TURQUOISE, Top4.W);
		ghosts[3] = PacManApp.THEME.spr_ghostColored(GhostColor.ORANGE, Top4.W);
		pill = true;
		tf.setWidth(88);
		tf.setHeight(16);
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
		pillTimer = app().clock.sec(0.5f);
	}

	@Override
	public void update() {
		tf.move();
		if (pillTimer > 0) {
			--pillTimer;
		}
		if (pillTimer == 0) {
			pill = !pill;
			pillTimer = app().clock.sec(0.5f);
		}
	}

	@Override
	public void start() {
		init();
		tf.setVelocityX(-1.2f);
		PacManApp.THEME.snd_siren().loop();
	}

	@Override
	public void stop() {
		tf.setVelocityX(0);
		PacManApp.THEME.snd_siren().stop();
	}

	@Override
	public boolean isCompleted() {
		return tf.getX() < endPosition.x;
	}

	@Override
	public void draw(Graphics2D g) {
		int x = 0;
		g.translate(tf.getX(), tf.getY());
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
		g.translate(-tf.getX(), -tf.getY());
	}
}