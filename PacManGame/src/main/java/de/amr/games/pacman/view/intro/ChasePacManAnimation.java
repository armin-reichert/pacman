package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.Animation;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;

public class ChasePacManAnimation extends Entity implements Animation {

	private final Theme theme;
	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];
	private int pillTimer;
	private Vector2f startPosition;
	private Vector2f endPosition;
	private boolean pill;

	public ChasePacManAnimation(Theme theme) {
		this.theme = theme;
		pacMan = theme.spr_pacManWalking(W);
		ghosts[0] = theme.spr_ghostColored(GhostColor.RED, W);
		ghosts[1] = theme.spr_ghostColored(GhostColor.PINK, W);
		ghosts[2] = theme.spr_ghostColored(GhostColor.CYAN, W);
		ghosts[3] = theme.spr_ghostColored(GhostColor.ORANGE, W);
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
		tf.setVelocityX(-0.8f);
		theme.snd_ghost_chase().loop();
	}

	@Override
	public void stop() {
		tf.setVelocityX(0);
		theme.snd_ghost_chase().stop();
	}

	@Override
	public boolean isComplete() {
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