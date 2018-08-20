package de.amr.games.pacman.view;

import static de.amr.games.pacman.model.Game.TS;
import static de.amr.games.pacman.view.PacManGameUI.SPRITES;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Animation;
import de.amr.easy.game.sprite.CyclicAnimation;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.game.Cast;
import de.amr.games.pacman.model.Maze;

public class MazePanel extends GameEntity {

	private final Maze maze;
	private final Cast actors;
	private final Animation energizerBlinking;
	private final Sprite s_maze_normal;
	private final Sprite s_maze_flashing;
	private boolean flashing;
	private int bonusTimer;

	public MazePanel(Maze maze, Cast actors) {
		this.maze = maze;
		this.actors = actors;
		s_maze_normal = SPRITES.mazeFull();
		s_maze_flashing = SPRITES.mazeFlashing();
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(500);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_maze_normal, s_maze_flashing);
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? s_maze_flashing : s_maze_normal;
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				actors.removeBonus();
			}
		}
		energizerBlinking.update();
	}

	@Override
	public int getWidth() {
		return maze.numCols() * TS;
	}

	@Override
	public int getHeight() {
		return maze.numRows() * TS;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public void setBonusTimer(int ticks) {
		bonusTimer = ticks;
	}

	@Override
	public void enableAnimation(boolean enable) {
		super.enableAnimation(enable);
		energizerBlinking.setEnabled(enable);
	}

	@Override
	public void draw(Graphics2D g) {
		if (flashing) {
			g.translate(tf.getX(), tf.getY());
			s_maze_flashing.draw(g);
			g.translate(-tf.getX(), -tf.getY());
		} else {
			g.translate(tf.getX(), tf.getY());
			s_maze_normal.draw(g);
			g.translate(-tf.getX(), -tf.getY());
			maze.tiles().forEach(tile -> {
				if (maze.isEatenFood(tile) || maze.isEnergizer(tile) && energizerBlinking.currentFrame() % 2 != 0) {
					g.translate(tile.col * TS, tile.row * TS);
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, TS, TS);
					g.translate(-tile.col * TS, -tile.row * TS);
				}
			});
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}
}