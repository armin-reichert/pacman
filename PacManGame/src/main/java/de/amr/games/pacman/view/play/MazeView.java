package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.PacManGame;

/**
 * Displays the maze and handles animations like blinking energizers.
 * 
 * @author Armin Reichert
 */
public class MazeView extends Entity {

	private final PacManGame game;
	private final Animation energizerBlinking;
	private boolean flashing;

	public MazeView(PacManGame game) {
		this.game = game;
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		energizerBlinking.setEnabled(false);
		sprites.set("normal", game.theme.spr_fullMaze());
		sprites.set("flashing", game.theme.spr_flashingMaze());
		setFlashing(false);
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
		g.setColor(game.theme.color_mazeBackground());
		g.fillRect(0, 0, currentSprite.getWidth(), currentSprite.getHeight());
		currentSprite.draw(g);
		g.translate(-tf.getX(), -tf.getY());
		if (!flashing) {
			energizerBlinking.update();
			game.maze.tiles().forEach(tile -> {
				if (game.maze.containsEatenFood(tile)
						|| game.maze.containsEnergizer(tile) && energizerBlinking.currentFrame() == 1) {
					g.setColor(game.theme.color_mazeBackground());
					g.fillRect(tile.col * TS, tile.row * TS, TS, TS);
				}
			});
		}
	}
}