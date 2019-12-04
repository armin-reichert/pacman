package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Displays the maze and handles animations like blinking energizers.
 * 
 * @author Armin Reichert
 */
public class MazeView extends Entity {

	private final PacManGame game;
	private PacManTheme theme;
	private final Animation energizerBlinking;
	private boolean flashing;

	public MazeView(PacManGame game, PacManTheme theme) {
		this.game = game;
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		energizerBlinking.setEnabled(false);
		setFlashing(false);
		setTheme(theme);
	}

	public void setTheme(PacManTheme theme) {
		this.theme = theme;
		sprites.set("normal", theme.spr_fullMaze());
		sprites.set("flashing", theme.spr_flashingMaze());
	}

	public void setFlashing(boolean flashing) {
		this.flashing = flashing;
		sprites.select(flashing ? "flashing" : "normal");
	}

	public void enableAnimation(boolean enabled) {
		sprites.enableAnimation(enabled);
		energizerBlinking.setEnabled(enabled);
	}

	@Override
	public void draw(Graphics2D g) {
		Sprite currentSprite = sprites.current().get();
		g.translate(tf.getX(), tf.getY());
		// we must draw the background because the maze sprite is transparent
		g.setColor(theme.color_mazeBackground());
		g.fillRect(0, 0, currentSprite.getWidth(), currentSprite.getHeight());
		currentSprite.draw(g);
		g.translate(-tf.getX(), -tf.getY());
		if (!flashing) {
			energizerBlinking.update();
			game.maze.tiles().forEach(tile -> {
				if (game.maze.containsEatenFood(tile)
						|| game.maze.containsEnergizer(tile) && energizerBlinking.currentFrame() == 1) {
					g.setColor(theme.color_mazeBackground());
					g.fillRect(tile.col * TS, tile.row * TS, TS, TS);
				}
			});
		}
	}
}