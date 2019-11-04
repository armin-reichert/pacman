package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.PacManGame;

/**
 * Displays the maze and handles animations like blinking energizers.
 * 
 * @author Armin Reichert
 */
public class MazeView extends SpriteEntity {

	private final PacManGame game;
	private final Animation energizerBlinking;
	private boolean flashing;

	public MazeView(PacManGame game) {
		this.game = game;
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(500);
		energizerBlinking.setEnabled(false);
		sprites.set("normal", game.theme.spr_fullMaze());
		sprites.set("flashing", game.theme.spr_flashingMaze());
		setFlashing(false);
	}

	@Override
	public void init() {
		setFlashing(false);
	}

	@Override
	public void update() {
		energizerBlinking.update();
	}

	public void setFlashing(boolean state) {
		flashing = state;
		sprites.select(flashing ? "flashing" : "normal");
	}

	public void enableSprites(boolean enable) {
		sprites.enableAnimation(enable);
		energizerBlinking.setEnabled(enable);
	}

	@Override
	public void draw(Graphics2D g) {
		Sprite mazeSprite = sprites.current().get();
		g.setColor(game.theme.color_mazeBackground());
		g.translate(tf.getX(), tf.getY());
		// we must draw the background because the maze sprite is transparent
		g.fillRect(0, 0, mazeSprite.getWidth(), mazeSprite.getHeight());
		mazeSprite.draw(g);
		g.translate(-tf.getX(), -tf.getY());
		if (!flashing) {
			// hide eaten pellets and turned off energizers
			game.maze.tiles().forEach(tile -> {
				if (game.maze.containsEatenFood(tile)
						|| game.maze.containsEnergizer(tile) && energizerBlinking.currentFrame() != 0) {
					g.fillRect(tile.col * TS, tile.row * TS, TS, TS);
				}
			});
		}
	}
}