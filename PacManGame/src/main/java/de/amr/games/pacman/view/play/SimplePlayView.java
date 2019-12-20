package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.DEAD;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;

import de.amr.easy.game.ui.sprites.Animation;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.PacManGameView;
import de.amr.games.pacman.view.Pen;

/**
 * Simple play view providing core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements PacManGameView, Controller {

	protected Animation energizerBlinking;
	protected boolean mazeFlashing;
	protected Image lifeImage;
	protected Sprite fullMazeSprite;
	protected Sprite flashingMazeSprite;
	protected PacManGameCast cast;
	protected Dimension viewSize;
	protected boolean showScores;
	protected String messageText;
	protected Color messageColor;

	public SimplePlayView(PacManGameCast cast) {
		this.cast = cast;
		cast.addThemeListener(this);
		viewSize = new Dimension(app().settings.width, app().settings.height);
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		setTheme(cast.theme());
	}

	@Override
	public PacManGameCast cast() {
		return cast;
	}

	@Override
	public PacManGame game() {
		return cast.game;
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
	}

	@Override
	public PacManTheme theme() {
		return cast.theme();
	}

	public void setTheme(PacManTheme theme) {
		lifeImage = theme.spr_pacManWalking(3).frame(1);
		fullMazeSprite = theme.spr_fullMaze();
		flashingMazeSprite = theme.spr_flashingMaze();
	}

	public void setShowScores(boolean showScores) {
		this.showScores = showScores;
	}

	@Override
	public void init() {
		energizerBlinking.setEnabled(false);
		mazeFlashing = false;
		messageColor = Color.YELLOW;
		clearMessage();
	}

	@Override
	public void update() {
		if (mazeFlashing) {
			return;
		}
		energizerBlinking.update();
	}

	@Override
	public void propertyChange(PropertyChangeEvent change) {
		if ("theme".equals(change.getPropertyName())) {
			PacManTheme theme = (PacManTheme) change.getNewValue();
			setTheme(theme);
		}
	}

	public void message(String message, Color color) {
		this.messageText = message;
		this.messageColor = color;
	}

	public void clearMessage() {
		messageText = null;
	}

	public void enableAnimations(boolean state) {
		flashingMazeSprite.enableAnimation(state);
		cast.ghostsOnStage().forEach(ghost -> ghost.sprites.enableAnimation(state));
	}

	public void mazeFlashing(boolean b) {
		mazeFlashing = b;
	}
	
	public void energizerBlinking(boolean b) {
		energizerBlinking.setEnabled(b);
	}

	@Override
	public void draw(Graphics2D g) {
		drawMazeBackground(g);
		drawMaze(g);
		drawActors(g);
		drawInfoText(g);
		drawScores(g);
	}

	protected void drawMazeBackground(Graphics2D g) {
		g.setColor(cast.theme().color_mazeBackground());
		g.fillRect(0, 0, maze().numCols * Tile.SIZE, maze().numRows * Tile.SIZE);
	}

	protected Color cellBackground(int col, int row) {
		return Color.BLACK;
	}

	protected void drawMaze(Graphics2D g) {
		Sprite mazeSprite = mazeFlashing ? flashingMazeSprite : fullMazeSprite;
		g.translate(0, 3 * Tile.SIZE);
		mazeSprite.draw(g);
		g.translate(0, -3 * Tile.SIZE);
		if (mazeFlashing) {
			return;
		}
		// hide tiles with eaten pellets
		maze().tiles().filter(Tile::containsEatenFood).forEach(tile -> {
			g.setColor(cellBackground(tile.col, tile.row));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// hide energizers when animation is in blank state
		if (energizerBlinking.currentFrame() == 1) {
			Arrays.stream(maze().energizers).forEach(tile -> {
				g.setColor(cellBackground(tile.col, tile.row));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw door open when ghost is passing through
		if (cast.ghostsOnStage().anyMatch(ghost -> ghost.tile().isDoor())) {
			g.setColor(cast.theme().color_mazeBackground());
			g.fillRect(maze().doorLeft.x(), maze().doorLeft.y(), 2 * Tile.SIZE, Tile.SIZE);
		}
	}

	protected void drawActors(Graphics2D g) {
		cast.bonus().ifPresent(bonus -> {
			bonus.draw(g);
		});
		if (cast.onStage(cast.pacMan)) {
			cast.pacMan.draw(g);
		}
		// draw dead ghosts (numbers) under living ghosts
		cast.ghostsOnStage().filter(ghost -> ghost.is(DEAD)).forEach(ghost -> ghost.draw(g));
		cast.ghostsOnStage().filter(ghost -> !ghost.is(DEAD)).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores) {
			return;
		}
		Pen pen = new Pen(g);
		pen.font(cast.theme().fnt_text(10));

		// Points
		pen.color(Color.YELLOW);
		pen.draw("SCORE", 1, 1);
		pen.draw(String.format("LEVEL%2d", game().level.number), 22, 1);
		pen.color(Color.WHITE);
		pen.draw(String.format("%07d", game().score), 1, 2);

		// Highscore
		pen.color(Color.YELLOW);
		pen.draw("HIGHSCORE", 10, 1);
		pen.color(Color.WHITE);
		pen.draw(String.format("%07d", game().hiscore.points), 10, 2);
		pen.draw(String.format("L%d", game().hiscore.levelNumber), 16, 2);

		// Remaining pellets
		g.setColor(Color.PINK);
		g.fillRect(22 * Tile.SIZE + 2, Tile.SIZE + 2, 4, 4);
		pen.color(Color.WHITE);
		pen.draw(String.format("%d", game().numPelletsRemaining()), 23, 2);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		for (int i = 0, x = imageSize; i < game().lives; ++i, x += imageSize) {
			g.drawImage(lifeImage, x, viewSize.height - imageSize, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		int x = viewSize.width - (game().levelSymbols.size() + 1) * imageSize;
		for (BonusSymbol symbol : game().levelSymbols) {
			Image image = cast.theme().spr_bonusSymbol(symbol).frame(0);
			g.drawImage(image, x, viewSize.height - imageSize, imageSize, imageSize, null);
			x += imageSize;
		}
	}

	protected void drawInfoText(Graphics2D g) {
		if (messageText != null) {
			Pen pen = new Pen(g);
			pen.font(cast.theme().fnt_text(11));
			pen.color(messageColor);
			pen.hcenter(messageText, viewSize.width, 21);
		}
	}
}