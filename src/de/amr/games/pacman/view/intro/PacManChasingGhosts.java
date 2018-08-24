package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

public class PacManChasingGhosts extends GameEntity {

	private Sprite pacMan;
	private float pacManX;
	private List<Sprite> ghosts = new ArrayList<>();
	private int ghostsKilled;
	private int showPointsTimer;
	private int gap = 17;

	public PacManChasingGhosts() {
		pacMan = PacManTheme.ASSETS.pacManWalking(Top4.E);
	}
	
	public void start() {
		init();
		tf.setVelocityX(.8f);
	}
	
	public void stop() {
		tf.setVelocityX(0);
		init();
	}
	
	public boolean isComplete() {
		return tf.getX() > 224;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < ghosts.size(); ++i) {
			g.translate(gap * (i + 1), 0);
			ghosts.get(i).draw(g);
			g.translate(-gap * (i + 1), 0);
		}
		g.translate(pacManX, 0);
		pacMan.draw(g);
		g.translate(-pacManX, 0);
		g.translate(-tf.getX(), -tf.getY());
	}

	@Override
	public void init() {
		ghosts.clear();
		for (int i = 0; i < 4; i++) {
			ghosts.add(PacManTheme.ASSETS.ghostFrightened());
		}
		tf.moveTo(-5 * gap, 200);
		pacManX = 0;
		ghostsKilled = 0;
	}

	@Override
	public void update() {
		if (showPointsTimer > 0) {
			showPointsTimer -= 1;
			return;
		}
		if (tf.getVelocityX() > 0) {
			tf.move();
			if (tf.getX() < 0) {
				return;
			}
			pacManX += 0.6f;
			if (ghostsKilled < 4) {
				int x = (int) pacManX + 4;
				if (x % gap == 0) {
					pacManX += 1;
					ghosts.remove(ghostsKilled);
					ghosts.add(ghostsKilled, PacManTheme.ASSETS.greenNumber(ghostsKilled));
					++ghostsKilled;
					showPointsTimer = 10;
				}
			}
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