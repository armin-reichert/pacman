package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.view.core.ViewAnimation;

public class ChasePacManAnimation extends GameEntity implements ViewAnimation {

	private final Sprite pacMan;
	private final Sprite ghosts[] = new Sprite[4];
	private int pillTimer;
	private Vector2f startPosition;
	private Vector2f endPosition;
	private boolean pill;

	public ChasePacManAnimation() {
		pacMan = THEME.pacManWalking(Top4.W);
		ghosts[0] = THEME.ghostColored(GhostColor.RED, Top4.W);
		ghosts[1] = THEME.ghostColored(GhostColor.PINK, Top4.W);
		ghosts[2] = THEME.ghostColored(GhostColor.TURQUOISE, Top4.W);
		ghosts[3] = THEME.ghostColored(GhostColor.ORANGE, Top4.W);
		pill = true;
	}

	public void setStartPosition(float x, float y) {
		this.startPosition = Vector2f.of(x, y);
	}

	public void setEndPosition(float x, float y) {
		this.endPosition = Vector2f.of(x, y);
	}

	@Override
	public void init() {
		tf.moveTo(startPosition);
		pillTimer = PULSE.secToTicks(0.5f);
	}

	@Override
	public void update() {
		tf.move();
		if (pillTimer > 0) {
			--pillTimer;
		}
		if (pillTimer == 0) {
			pill = !pill;
			pillTimer = PULSE.secToTicks(0.5f);
		}
	}

	@Override
	public void startAnimation() {
		init();
		tf.setVelocityX(-1.2f);
		THEME.soundSiren().loop();
	}

	@Override
	public void stopAnimation() {
		tf.setVelocityX(0);
		THEME.soundSiren().stop();
	}

	@Override
	public boolean isAnimationCompleted() {
		return tf.getX() < endPosition.x;
	}

	@Override
	public int getWidth() {
		return 88;
	}
	
	@Override
	public int getHeight() {
		return 16;
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

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}
}