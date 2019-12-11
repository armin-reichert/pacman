package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.awt.Color;
import java.awt.Dimension;
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

/**
 * Simple play view providing core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements View, Controller {

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
		g.translate(0, 3 * Maze.TS);
		g.fillRect(0, 0, mazeSprite.getWidth(), mazeSprite.getHeight());
		mazeSprite.draw(g);
		g.translate(0, -3 * Maze.TS);
		if (mazeFlashing) {
			return;
		}
		// hide tiles with eaten pellets
		maze.tiles().filter(maze::containsEatenFood).forEach(tile -> {
			g.setColor(cast.theme.color_mazeBackground());
			g.fillRect(tile.col * Maze.TS, tile.row * Maze.TS, Maze.TS, Maze.TS);
		});
		// hide energizers when animation is in blank state
		if (energizerBlinking.currentFrame() == 1) {
			maze.energizerTiles().forEach(tile -> {
				g.setColor(cast.theme.color_mazeBackground());
				g.fillRect(tile.col * Maze.TS, tile.row * Maze.TS, Maze.TS, Maze.TS);
			});
		}
		// draw door open when ghost is passing through
		if (cast.activeGhosts().anyMatch(ghost -> maze.isDoor(ghost.tile()))) {
			g.setColor(cast.theme.color_mazeBackground());
			g.fillRect(maze.doorLeft.col * Maze.TS, maze.doorLeft.row * Maze.TS, 2 * Maze.TS, Maze.TS);
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
		// Points score
		g.setFont(cast.theme.fnt_text());
		g.setColor(Color.YELLOW);
		g.drawString("SCORE", Maze.TS, Maze.TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", game.score), Maze.TS, 2 * Maze.TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("LEVEL %2d", game.level.number), 22 * Maze.TS, Maze.TS);

		// Highscore
		g.setColor(Color.YELLOW);
		g.drawString("HIGH", 10 * Maze.TS, Maze.TS);
		g.drawString("SCORE", 14 * Maze.TS, Maze.TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", game.hiscorePoints), 10 * Maze.TS, 2 * Maze.TS);
		g.drawString(String.format("L%d", game.hiscoreLevel), 16 * Maze.TS, 2 * Maze.TS);

		// Remaining pellets score
		g.setColor(Color.PINK);
		g.fillRect(22 * Maze.TS + 2, Maze.TS + 2, 4, 4);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%d", game.numPelletsRemaining()), 23 * Maze.TS, 2 * Maze.TS);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int imageSize = 2 * Maze.TS;
		for (int i = 0, x = imageSize; i < game.lives; ++i, x += imageSize) {
			g.drawImage(lifeImage, x, size.height - imageSize, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * Maze.TS;
		int x = fullMazeSprite.getWidth() - (game.levelSymbols.size() + 1) * imageSize;
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
		g2.translate((mazeWidth - box.width) / 2, (maze.bonusTile.row + 1) * Maze.TS);
		g2.drawString(message, 0, 0);
		g2.dispose();
	}
}