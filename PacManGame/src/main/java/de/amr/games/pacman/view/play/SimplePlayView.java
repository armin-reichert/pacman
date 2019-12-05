package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.PacManGame.TS;

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
import de.amr.games.pacman.model.PacManGame;
import de.amr.graph.grid.impl.Top4;

/**
 * Simple play view providing core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements View, Controller {

	public boolean showScores;

	protected final PacManGame game;
	protected final PacManGameCast cast;
	protected final Dimension size;

	protected Image lifeImage;
	protected Sprite fullMazeSprite, flashingMazeSprite;
	protected Animation energizerBlinking;

	protected boolean mazeFlashing;
	protected int bonusDisplayTicks;
	protected String infoText;
	protected Color infoTextColor;

	public SimplePlayView(PacManGame game, PacManGameCast cast) {
		this.game = game;
		this.cast = cast;
		size = new Dimension(app().settings.width, app().settings.height);
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		updateTheme();
	}

	@Override
	public void init() {
		energizerBlinking.setEnabled(false);
		mazeFlashing = false;
		bonusDisplayTicks = 0;
		infoText = null;
		infoTextColor = Color.YELLOW;
	}

	@Override
	public void update() {
		if (mazeFlashing) {
			return;
		}
		if (cast.bonus.isPresent() && bonusDisplayTicks > 0) {
			bonusDisplayTicks -= 1;
			if (bonusDisplayTicks == 0) {
				cast.clearBonus();
			}
		}
		energizerBlinking.update();
	}

	public void updateTheme() {
		lifeImage = cast.theme.spr_pacManWalking(Top4.W).frame(1);
		fullMazeSprite = cast.theme.spr_fullMaze();
		flashingMazeSprite = cast.theme.spr_flashingMaze();
	}

	public void enableAnimations(boolean state) {
		flashingMazeSprite.enableAnimation(state);
		cast.actors().forEach(actor -> actor.sprites.enableAnimation(state));
	}

	public void startEnergizerBlinking() {
		energizerBlinking.setEnabled(true);
	}

	public void stopEnergizerBlinking() {
		energizerBlinking.setEnabled(false);
	}

	public void startMazeFlashing() {
		mazeFlashing = true;
	}

	public void stopMazeFlashing() {
		mazeFlashing = false;
	}

	public void displayBonus(int ticks) {
		cast.bonus.ifPresent(bonus -> {
			bonusDisplayTicks = ticks;
			LOGGER.info(() -> String.format("Display %s for %d ticks (%.2f seconds)", bonus, ticks, ticks / 60f));
		});
	}

	public void consumeBonus(int ticks) {
		cast.bonus.ifPresent(bonus -> {
			bonus.changeIntoNumber();
			bonusDisplayTicks = ticks;
		});
	}

	public void showInfoText(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void clearInfoText() {
		infoText = null;
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
		g.translate(0, 3 * TS);
		g.fillRect(0, 0, mazeSprite.getWidth(), mazeSprite.getHeight());
		mazeSprite.draw(g);
		g.translate(0, -3 * TS);
		if (mazeFlashing) {
			return;
		}
		// hide tiles with eaten pellets
		game.maze.tiles().filter(game.maze::containsEatenFood).forEach(tile -> {
			g.setColor(cast.theme.color_mazeBackground());
			g.fillRect(tile.col * TS, tile.row * TS, TS, TS);
		});
		// hide energizers when animation is in blank state
		if (energizerBlinking.currentFrame() == 1) {
			game.maze.energizerTiles().forEach(tile -> {
				g.setColor(cast.theme.color_mazeBackground());
				g.fillRect(tile.col * TS, tile.row * TS, TS, TS);
			});
		}
	}

	protected void drawActors(Graphics2D g) {
		if (cast.bonus.isPresent() && bonusDisplayTicks > 0) {
			cast.bonus.get().draw(g);
		}
		if (cast.pacMan.isActive()) {
			cast.pacMan.draw(g);
		}
		// draw dying ghosts (numbers) under non-dying ghosts
		cast.activeGhosts().sorted((g1, g2) -> {
			GhostState s1 = g1.getState(), s2 = g2.getState();
			return s1 == s2 ? 0 : s1 == GhostState.DYING ? -1 : 1;
		}).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores) {
			return;
		}
		// Points score
		int score = game.score.getPoints();
		g.setFont(cast.theme.fnt_text());
		g.setColor(Color.YELLOW);
		g.drawString("SCORE", TS, TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", score), TS, 2 * TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("LEVEL %2d", game.levelNumber), 22 * TS, TS);

		// Highscore
		g.setColor(Color.YELLOW);
		g.drawString("HIGH", 10 * TS, TS);
		g.drawString("SCORE", 14 * TS, TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", game.score.getHiscorePoints()), 10 * TS, 2 * TS);
		g.drawString(String.format("L%d", game.score.getHiscoreLevel()), 16 * TS, 2 * TS);

		// Remaining pellets score
		g.setColor(Color.PINK);
		g.fillRect(22 * TS + 2, TS + 2, 4, 4);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%d", game.numPelletsRemaining()), 23 * TS, 2 * TS);

		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int x = 0;
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(lifeImage, x, size.height - 2 * TS, null);
			x += 2 * TS;
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * TS;
		int x = fullMazeSprite.getWidth() - (game.levelCounter.size() + 1) * imageSize;
		for (BonusSymbol symbol : game.levelCounter) {
			Image image = cast.theme.spr_bonusSymbol(symbol).frame(0);
			g.drawImage(image, x, size.height - imageSize, imageSize, imageSize, null);
			x += imageSize;
		}
	}

	protected void drawInfoText(Graphics2D g) {
		if (infoText == null) {
			return;
		}
		int mazeWidth = fullMazeSprite.getWidth();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(cast.theme.fnt_text(14));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((mazeWidth - box.width) / 2, (game.maze.bonusTile.row + 1) * TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}
}