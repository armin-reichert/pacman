package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.entity.SpriteBasedGameEntity;
import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.model.Maze;

public class MazeView extends SpriteBasedGameEntity {

	private final Maze maze;
	private final Animation energizerBlinking;
	private boolean flashing;
	private Bonus bonus;
	private int bonusTimer;

	public MazeView(Maze maze) {
		this.maze = maze;
		sprites.set("s_normal", PacManApp.theme.spr_fullMaze());
		sprites.set("s_flashing", PacManApp.theme.spr_flashingMaze());
		sprites.select("s_normal");
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(500);
		energizerBlinking.setEnabled(false);
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
		bonus.placeAtTile(maze.getBonusTile(), TS / 2, 0);
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
			maze.tiles().forEach(tile -> {
				if (maze.isEatenFood(tile) || maze.isEnergizer(tile) && energizerBlinking.currentFrame() % 2 != 0) {
					g.translate(tile.col * TS, tile.row * TS);
					g.setColor(app().settings.bgColor);
					g.fillRect(0, 0, TS + 1, TS + 1);
					g.translate(-tile.col * TS, -tile.row * TS);
				}
			});
			if (bonus != null) {
				bonus.draw(g);
			}
		}
	}
}