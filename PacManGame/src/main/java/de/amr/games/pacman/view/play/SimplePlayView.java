package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.CROWDED;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.EMPTY;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.FLASHING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.core.BaseView;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.StateMachine;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView extends BaseView {

	public enum MazeMode {
		EMPTY, CROWDED, FLASHING
	}

	public final Game game;
	public final MazeView mazeView;

	private String messageText = "";
	private Color messageColor = Color.YELLOW;
	private int messageFontSize = 8;
	private int messageRow = 21;

	public SimplePlayView(Game game, Theme theme) {
		super(theme);
		this.game = game;
		mazeView = new MazeView();
		game.pacMan.takeClothes(theme);
		game.blinky.takeClothes(theme, Theme.RED_GHOST);
		game.pinky.takeClothes(theme, Theme.PINK_GHOST);
		game.inky.takeClothes(theme, Theme.CYAN_GHOST);
		game.clyde.takeClothes(theme, Theme.ORANGE_GHOST);
	}

	@Override
	public void init() {
		mazeView.init();
		clearMessage();
	}

	@Override
	public void update() {
		mazeView.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawBackground(g);
		drawScores(g);
		drawMaze(g);
		drawMessage(g);
		drawActors(g);
	}

	public void showMessage(String text, Color color, int fontSize) {
		messageText = text;
		messageColor = color;
		messageFontSize = fontSize;
	}

	public void showMessage(String text, Color color) {
		messageText = text;
		messageColor = color;
		messageFontSize = 8;
	}

	public void clearMessage() {
		messageText = "";
	}

	public void enableGhostAnimations(boolean enabled) {
		game.ghosts().flatMap(ghost -> ghost.sprites.values()).forEach(sprite -> sprite.enableAnimation(enabled));
	}

	protected Color tileColor(Tile tile) {
		return theme.color_mazeBackground();
	}

	protected void drawBackground(Graphics2D g) {
		g.setColor(theme.color_mazeBackground());
		g.fillRect(0, 0, width, height);
	}

	protected void drawMaze(Graphics2D g) {
		mazeView.draw(g);
	}

	protected void drawMessage(Graphics2D g) {
		if (messageText != null && messageText.trim().length() > 0) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text());
				pen.fontSize(messageFontSize);
				pen.color(messageColor);
				pen.hcenter(messageText, width, messageRow, Tile.SIZE);
			}
		}
	}

	protected void drawActors(Graphics2D g) {
		drawActor(g, game.bonus, game.bonus.sprites);
		drawActor(g, game.pacMan, game.pacMan.sprites);
		// draw dead ghosts (as number or eyes) under living ghosts
		game.ghostsOnStage().filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> drawActor(g, ghost, ghost.sprites));
		game.ghostsOnStage().filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> drawActor(g, ghost, ghost.sprites));
	}

	protected void drawScores(Graphics2D g) {
		int topMargin = 3;
		int lineOffset = 2;
		Color hilight = Color.YELLOW;
		int col;
		g.translate(0, topMargin); // margin between score and upper window border
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());

			// Game score
			col = 1;
			pen.color(hilight);
			pen.drawAtGridPosition("Score".toUpperCase(), col, 0, Tile.SIZE);

			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%07d", game.score), col, 1, Tile.SIZE);
			pen.up(lineOffset);

			// Highscore
			col = 9;
			pen.color(hilight);
			pen.drawAtGridPosition("Highscore".toUpperCase(), col, 0, Tile.SIZE);
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%07d", game.hiscore.points), col, 1, Tile.SIZE);
			pen.drawAtGridPosition(String.format("L%02d", game.hiscore.levelNumber), col + 7, 1, Tile.SIZE);
			pen.up(lineOffset);

			col = 20;
			pen.color(hilight);
			pen.drawAtGridPosition(String.format("Level".toUpperCase()), col, 0, Tile.SIZE);
			// Level number
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%02d", game.level.number), col, 1, Tile.SIZE);
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.translate(0, (topMargin + lineOffset) - 2);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.fillOval((col + 2) * Tile.SIZE + 2, Tile.SIZE, 4, 4); // dot image
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.translate(0, -(topMargin + lineOffset) + 2);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%03d", game.remainingFoodCount()), col + 3, 1, Tile.SIZE);
			pen.up(lineOffset);
		}
		g.translate(0, -topMargin);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int sz = 2 * Tile.SIZE;
		Image pacManLookingLeft = theme.spr_pacManWalking(LEFT).frame(1);
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.drawImage(pacManLookingLeft, x, height - sz, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int max = 7;
		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int sz = 2 * Tile.SIZE; // image size
		for (int i = 0, x = width - 2 * sz; i < n; ++i, x -= sz) {
			Symbol symbol = game.levelCounter.get(first + i);
			g.drawImage(theme.spr_bonusSymbol(symbol).frame(0), x, height - sz, sz, sz, null);
		}
	}

	/**
	 * Inner class realizing the maze view which can be in any of states EMPTY, CROWDED or FLASHING.
	 */
	public class MazeView extends StateMachine<MazeMode, Void> implements View {

		public Sprite spriteEmptyMaze, spriteFullMaze, spriteFlashingMaze;
		public SpriteAnimation energizersBlinking;

		public MazeView() {
			super(MazeMode.class);
			spriteFullMaze = theme.spr_fullMaze();
			spriteEmptyMaze = theme.spr_emptyMaze();
			spriteFlashingMaze = theme.spr_flashingMaze();
			energizersBlinking = new CyclicAnimation(2);
			energizersBlinking.setFrameDuration(150);
			game.bonus.tf.setPosition(game.maze.bonusTile.centerX(), game.maze.bonusTile.y());
			//@formatter:off
			beginStateMachine()
				.description("[Maze View]")
				.initialState(CROWDED)
				.states()
					.state(CROWDED)
						.onEntry(() -> energizersBlinking.setEnabled(false))
						.onTick(() -> energizersBlinking.update())
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
					g.setColor(tileColor(tile));
					g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
				});
				// hide active energizers when blinking animation is in dark phase
				if (energizersBlinking.currentFrame() == 1) {
					game.maze.playingArea().filter(game.maze::isEnergizer).forEach(tile -> {
						g.setColor(tileColor(tile));
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