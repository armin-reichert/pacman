package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.Game.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Animation;
import de.amr.easy.game.sprite.CyclicAnimation;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.theme.PacManThemes;

public class MazeView extends GameEntityUsingSprites {

	private final Maze maze;
	private final Animation energizerBlinking;
	private final Sprite s_maze_normal;
	private final Sprite s_maze_flashing;
	private boolean flashing;
	private Bonus bonus;
	private int bonusTimer;

	public MazeView(Maze maze) {
		this.maze = maze;
		s_maze_normal = PacManThemes.THEME.mazeFull();
		s_maze_flashing = PacManThemes.THEME.mazeFlashing();
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(500);
		energizerBlinking.setEnabled(false);
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
				bonus = null;
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

	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
		bonus.placeAt(maze.getBonusTile(), TS / 2, 0);
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
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
			if (bonus != null) {
				bonus.draw(g);
			}
		}
	}

	@Override
	public void init() {
		bonus = null;
	}
}