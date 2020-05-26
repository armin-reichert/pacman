package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.CROWDED;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.EMPTY;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.FLASHING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.PacManGameView;
import de.amr.statemachine.core.StateMachine;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView extends PacManGameView {

	public enum MazeMode {
		EMPTY, CROWDED, FLASHING
	}

	public boolean showScores = true;
	public MazeView mazeView;
	private Image imageLife;

	public SimplePlayView(Game game, Theme theme) {
		super(game, theme);
		imageLife = theme.spr_pacManWalking(3).frame(1);
		mazeView = new MazeView();
	}

	@Override
	public void init() {
		super.init();
		mazeView.init();
	}

	@Override
	public void update() {
		mazeView.update();
	}

	@Override
	public void draw(Graphics2D g) {
		fillBackground(g, theme.color_mazeBackground());
		drawScores(g);
		drawMaze(g);
		drawMessage(g);
		drawActors(g);
	}

	public void enableGhostAnimations(boolean enabled) {
		game.ghostsOnStage().flatMap(ghost -> ghost.sprites.values()).forEach(sprite -> sprite.enableAnimation(enabled));
	}

	public float mazeFlashingSeconds() {
		return game.level.mazeNumFlashes * Theme.MAZE_FLASH_TIME_MILLIS / 1000f;
	}

	public void setEnergizersBlinking(boolean enabled) {
		mazeView.energizerBlinking.setEnabled(enabled);
	}

	protected Color bgColor(Tile tile) {
		return theme.color_mazeBackground();
	}

	protected void drawMaze(Graphics2D g) {
		mazeView.draw(g);
	}

	protected void drawActors(Graphics2D g) {
		drawActor(g, game.bonus, game.bonus.sprites);
		drawActor(g, game.pacMan, game.pacMan.sprites);
		// draw dead ghosts (points) below living ghosts
		game.ghostsOnStage().filter(ghost -> ghost.is(DEAD)).forEach(ghost -> drawActor(g, ghost, ghost.sprites));
		game.ghostsOnStage().filter(ghost -> !ghost.is(DEAD)).forEach(ghost -> drawActor(g, ghost, ghost.sprites));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores) {
			return;
		}
		Color hilight = Color.YELLOW;
		int col;
		g.translate(0, 3); // margin between score and upper window border
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());

			// Game score
			col = 1;
			pen.color(hilight);
			pen.drawAtGridPosition("Score".toUpperCase(), col, 0, Tile.SIZE);

			pen.down(1);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%07d", game.score), col, 1, Tile.SIZE);
			pen.up(1);

			// Highscore
			col = 9;
			pen.color(hilight);
			pen.drawAtGridPosition("Highscore".toUpperCase(), col, 0, Tile.SIZE);
			pen.down(1);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%07d", game.hiscore.points), col, 1, Tile.SIZE);
			pen.drawAtGridPosition(String.format("L%02d", game.hiscore.levelNumber), col + 7, 1, Tile.SIZE);
			pen.up(1);

			col = 20;
			pen.color(hilight);
			pen.drawAtGridPosition(String.format("Level".toUpperCase()), col, 0, Tile.SIZE);
			// Level number
			pen.down(1);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%02d", game.level.number), col, 1, Tile.SIZE);
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.fillRect((col + 2) * Tile.SIZE + 2, Tile.SIZE + 2, 3, 3); // dot image
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%03d", game.remainingFoodCount()), col + 3, 1, Tile.SIZE);
			pen.up(1);
		}
		g.translate(0, -3);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int sz = 2 * Tile.SIZE;
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.drawImage(imageLife, x, height() - sz, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int max = 7;
		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int sz = 2 * Tile.SIZE; // image size
		for (int i = 0, x = width() - 2 * sz; i < n; ++i, x -= sz) {
			Symbol symbol = game.levelCounter.get(first + i);
			g.drawImage(theme.spr_bonusSymbol(symbol).frame(0), x, height() - sz, sz, sz, null);
		}
	}

	/**
	 * Inner class realizing the maze view which can be in any of states EMPTY, CROWDED or FLASHING.
	 */
	public class MazeView extends StateMachine<MazeMode, Void> implements View {

		Sprite spriteEmptyMaze, spriteFullMaze, spriteFlashingMaze;
		SpriteAnimation energizerBlinking;

		public MazeView() {
			super(MazeMode.class);
			spriteFullMaze = theme.spr_fullMaze();
			spriteEmptyMaze = theme.spr_emptyMaze();
			spriteFlashingMaze = theme.spr_flashingMaze();
			energizerBlinking = new CyclicAnimation(2);
			energizerBlinking.setFrameDuration(150);
			game.bonus.tf.setPosition(game.maze.bonusTile.centerX(), game.maze.bonusTile.y());
			//@formatter:off
			beginStateMachine()
				.description("[Maze View]")
				.initialState(CROWDED)
				.states()
					.state(CROWDED)
						.onEntry(() -> energizerBlinking.setEnabled(false))
						.onTick(() -> energizerBlinking.update())
				.transitions()
			.endStateMachine();
			//@formatter:on
			getTracer().setLogger(Application.LOGGER);
		}

		@Override
		public void draw(Graphics2D g) {
			if (getState() == CROWDED) {
				spriteFullMaze.draw(g, 0, 3 * Tile.SIZE);
				// hide eaten food
				game.maze.playingArea().filter(game.maze::isEatenFood).forEach(tile -> {
					g.setColor(bgColor(tile));
					g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
				});
				// hide active energizers when blinking animation is in dark phase
				if (energizerBlinking.currentFrame() == 1) {
					game.maze.playingArea().filter(game.maze::isEnergizer).forEach(tile -> {
						g.setColor(bgColor(tile));
						g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
					});
				}
				// draw door open when any ghost is entering or leaving the house
				if (game.ghostsOnStage().anyMatch(ghost -> ghost.is(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE))) {
					g.setColor(theme.color_mazeBackground());
					g.fillRect(game.maze.ghostHouseDoorLeft.x(), game.maze.ghostHouseDoorLeft.y(), 2 * Tile.SIZE, Tile.SIZE);
				}
			} else if (getState() == EMPTY) {
				spriteEmptyMaze.draw(g, 0, 3 * Tile.SIZE);
			} else if (getState() == FLASHING) {
				spriteFlashingMaze.draw(g, 0, 3 * Tile.SIZE);
			}
		}
	}
}