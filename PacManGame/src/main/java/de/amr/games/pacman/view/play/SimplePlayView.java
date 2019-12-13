package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;

/**
 * Simple play view providing core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements View, Controller {

	public static class Pen {

		private Graphics2D g;
		public Font font;
		public Color color;

		public Pen(Graphics2D g) {
			this.g = (Graphics2D) g.create();
		}

		public void text(String s, int col, int row) {
			g.setColor(color);
			g.setFont(font);
			g.drawString(s, col * Tile.SIZE, row * Tile.SIZE);
		}
	}

	public final PacManGame game;
	public final Maze maze;
	public final PacManGameCast cast;
	public final Dimension size;
	public final Animation energizerBlinking;

	public boolean showScores;
	public boolean mazeFlashing;
	public String message;
	public Color textColor;

	public Image lifeImage;
	public Sprite fullMazeSprite;
	public Sprite flashingMazeSprite;

	public SimplePlayView(PacManGameCast cast) {
		this.cast = cast;
		this.game = cast.game;
		this.maze = game.maze;
		size = new Dimension(app().settings.width, app().settings.height);
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		updateTheme();
	}

	@Override
	public void init() {
		energizerBlinking.setEnabled(false);
		mazeFlashing = false;
		message = null;
		textColor = Color.YELLOW;
	}

	@Override
	public void update() {
		if (mazeFlashing) {
			return;
		}
		energizerBlinking.update();
	}

	public void updateTheme() {
		lifeImage = cast.theme.spr_pacManWalking(W).frame(1);
		fullMazeSprite = cast.theme.spr_fullMaze();
		flashingMazeSprite = cast.theme.spr_flashingMaze();
	}

	public void enableAnimations(boolean state) {
		flashingMazeSprite.enableAnimation(state);
		cast.activeGhosts().forEach(ghost -> ghost.sprites.enableAnimation(state));
	}

	@Override
	public void draw(Graphics2D g) {
		drawMaze(g);
		drawActors(g);
		drawInfoText(g);
		drawScores(g);
	}

	protected void drawMaze(Graphics2D g) {
		Sprite mazeSprite = mazeFlashing ? flashingMazeSprite : fullMazeSprite;
		// draw background because maze sprite is transparent
		g.setColor(cast.theme.color_mazeBackground());
		g.translate(0, 3 * Tile.SIZE);
		g.fillRect(0, 0, mazeSprite.getWidth(), mazeSprite.getHeight());
		mazeSprite.draw(g);
		g.translate(0, -3 * Tile.SIZE);
		if (mazeFlashing) {
			return;
		}
		// hide tiles with eaten pellets
		maze.tiles().filter(maze::containsEatenFood).forEach(tile -> {
			g.setColor(cast.theme.color_mazeBackground());
			g.fillRect(tile.col * Tile.SIZE, tile.row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
		});
		// hide energizers when animation is in blank state
		if (energizerBlinking.currentFrame() == 1) {
			maze.energizerTiles().forEach(tile -> {
				g.setColor(cast.theme.color_mazeBackground());
				g.fillRect(tile.col * Tile.SIZE, tile.row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
			});
		}
		// draw door open when ghost is passing through
		if (cast.activeGhosts().anyMatch(ghost -> maze.isDoor(ghost.tile()))) {
			g.setColor(cast.theme.color_mazeBackground());
			g.fillRect(maze.doorLeft.col * Tile.SIZE, maze.doorLeft.row * Tile.SIZE, 2 * Tile.SIZE, Tile.SIZE);
		}
	}

	protected void drawActors(Graphics2D g) {
		cast.bonus().ifPresent(bonus -> {
			bonus.draw(g);
		});
		if (cast.isActive(cast.pacMan)) {
			cast.pacMan.draw(g);
		}
		// draw dying ghosts (numbers) under non-dying ghosts
		cast.activeGhosts().filter(ghost -> ghost.getState() == GhostState.DYING).forEach(ghost -> ghost.draw(g));
		cast.activeGhosts().filter(ghost -> ghost.getState() != GhostState.DYING).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores) {
			return;
		}
		Pen pen = new Pen(g);
		pen.font = cast.theme.fnt_text();

		// Points
		pen.color = Color.YELLOW;
		pen.text("SCORE", 1, 1);
		pen.text(String.format("LEVEL %2d", game.level.number), 22, 1);
		pen.color = Color.WHITE;
		pen.text(String.format("%07d", game.score), 1, 2);

		// Highscore
		pen.color = Color.YELLOW;
		pen.text("HIGH", 10, 1);
		pen.text("SCORE", 14, 1);
		pen.color = Color.WHITE;
		pen.text(String.format("%07d", game.hiscore.points), 10, 2);
		pen.text(String.format("L%d", game.hiscore.levelNumber), 16, 2);

		// Remaining pellets
		g.setColor(Color.PINK);
		g.fillRect(22 * Tile.SIZE + 2, Tile.SIZE + 2, 4, 4);
		pen.color = Color.WHITE;
		pen.text(String.format("%d", game.numPelletsRemaining()), 23, 2);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		for (int i = 0, x = imageSize; i < game.lives; ++i, x += imageSize) {
			g.drawImage(lifeImage, x, size.height - imageSize, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		int x = fullMazeSprite.getWidth() - (game.levelSymbols.count() + 1) * imageSize;
		for (BonusSymbol symbol : game.levelSymbols) {
			Image image = cast.theme.spr_bonusSymbol(symbol).frame(0);
			g.drawImage(image, x, size.height - imageSize, imageSize, imageSize, null);
			x += imageSize;
		}
	}

	protected void drawInfoText(Graphics2D g) {
		if (message == null) {
			return;
		}
		int mazeWidth = fullMazeSprite.getWidth();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(cast.theme.fnt_text(14));
		g2.setColor(textColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(message, g2).getBounds();
		g2.translate((mazeWidth - box.width) / 2, (maze.bonusTile.row + 1) * Tile.SIZE);
		g2.drawString(message, 0, 0);
		g2.dispose();
	}
}