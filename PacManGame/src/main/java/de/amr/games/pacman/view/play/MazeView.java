package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Displays the maze, bonus symbol and handles animation of energizers and maze.
 * 
 * @author Armin Reichert
 */
public class MazeView extends SpriteEntity {

	private final Maze maze;
	private final Animation energizerBlinking;
	private boolean flashing;
	private Bonus bonus;
	private int bonusTimer;

	public MazeView(Maze maze) {
		this.maze = maze;
		sprites.set("s_normal", getTheme().spr_fullMaze());
		sprites.set("s_flashing", getTheme().spr_flashingMaze());
		sprites.select("s_normal");
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(500);
		energizerBlinking.setEnabled(false);
	}

	private PacManTheme getTheme() {
		return Application.app().settings.get("theme");
	}

	@Override
	public void init() {
		bonus = null;
		bonusTimer = 0;
		setFlashing(false);
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				bonus = null;
			}
		}
		energizerBlinking.update();
	}

	public void setFlashing(boolean on) {
		flashing = on;
		sprites.select(flashing ? "s_flashing" : "s_normal");
	}

	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
		Tile tile = maze.getBonusTile();
		bonus.tf.setPosition(tile.col * TS + TS / 2, tile.row * TS);
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public void setBonusTimer(int ticks) {
		bonusTimer = ticks;
	}

	public void enableSprites(boolean enable) {
		sprites.enableAnimation(enable);
		energizerBlinking.setEnabled(enable);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		sprites.current().draw(g);
		g.translate(-tf.getX(), -tf.getY());
		if (!flashing) {
			// hide eaten pellets and let energizer blink
			maze.tiles().forEach(tile -> {
				if (maze.isEatenFood(tile) || maze.isEnergizer(tile) && energizerBlinking.currentFrame() != 0) {
					g.translate(tile.col * TS, tile.row * TS);
					g.setColor(app().settings.bgColor);
					g.fillRect(0, 0, TS, TS);
					g.translate(-tile.col * TS, -tile.row * TS);
				}
			});
			if (bonus != null) {
				bonus.draw(g);
			}
		}
	}
}