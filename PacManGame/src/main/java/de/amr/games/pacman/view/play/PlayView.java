package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.graph.grid.impl.Top4;

/**
 * Simple play view without bells and whistles.
 * 
 * @author Armin Reichert
 */
public class PlayView implements View, Controller {

	public boolean showScores;
	protected final PacManGame game;
	protected final Dimension size;
	protected final MazeView mazeView;
	protected PacManTheme theme;
	protected Image lifeImage;
	protected String infoText;
	protected Color infoTextColor;
	protected int bonusTimer;

	public PlayView(PacManGame game) {
		this.game = game;
		size = new Dimension(app().settings.width, app().settings.height);
		mazeView = new MazeView(game);
		mazeView.tf.setPosition(0, 3 * TS);
	}

	@Override
	public void init() {
		game.removeBonus();
		bonusTimer = 0;
		mazeView.setFlashing(false);
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				game.removeBonus();
			}
		}
	}

	public void setTheme(PacManTheme theme) {
		this.theme = theme;
		lifeImage = theme.spr_pacManWalking(Top4.W).frame(1);
		mazeView.setTheme(theme);
		theme.apply(game.pacMan);
		theme.apply(game.blinky, GhostColor.RED);
		theme.apply(game.pinky, GhostColor.PINK);
		theme.apply(game.inky, GhostColor.CYAN);
		theme.apply(game.clyde, GhostColor.ORANGE);
	}

	public void enableAnimation(boolean enabled) {
		mazeView.enableAnimation(enabled);
		game.pacMan.sprites.enableAnimation(enabled);
		game.activeGhosts().forEach(ghost -> ghost.sprites.enableAnimation(enabled));
	}

	public void setBonusTimer(int ticks) {
		bonusTimer = ticks;
	}

	public void setBonus(BonusSymbol symbol, int value) {
		Bonus bonus = new Bonus(symbol, value, theme);
		game.setBonus(bonus);
		Tile bonusTile = game.maze.bonusTile;
		bonus.tf.setPosition(bonusTile.col * TS + TS / 2, bonusTile.row * TS);
	}

	public void setMazeFlashing(boolean flashing) {
		mazeView.setFlashing(flashing);
	}

	public void showInfoText(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void hideInfoText() {
		infoText = null;
	}

	@Override
	public void draw(Graphics2D g) {
		mazeView.draw(g);
		game.getBonus().ifPresent(bonus -> bonus.draw(g));
		drawActors(g);
		drawInfoText(g);
		drawScores(g);
	}

	protected void drawActors(Graphics2D g) {
		if (game.pacMan.isActive()) {
			game.pacMan.draw(g);
		}
		game.activeGhosts().filter(ghost -> ghost.getState() != GhostState.DYING).forEach(ghost -> ghost.draw(g));
		game.activeGhosts().filter(ghost -> ghost.getState() == GhostState.DYING).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (showScores) {
			// Points score
			int score = game.score.getPoints();
			g.setFont(theme.fnt_text());
			g.setColor(Color.YELLOW);
			g.drawString("SCORE", TS, TS);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%07d", score), TS, 2 * TS);
			g.setColor(Color.YELLOW);
			g.drawString(String.format("LEVEL %2d", game.level), 22 * TS, TS);

			// High score
			g.setColor(Color.YELLOW);
			g.drawString("HIGH", 10 * TS, TS);
			g.drawString("SCORE", 14 * TS, TS);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%07d", game.score.getHiscorePoints()), 10 * TS, 2 * TS);
			g.drawString(String.format("L%d", game.score.getHiscoreLevel()), 16 * TS, 2 * TS);

			// Food remaining score
			g.setColor(Color.PINK);
			g.fillRect(22 * TS + 2, TS + 2, 4, 4);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%d", game.numPelletsRemaining()), 23 * TS, 2 * TS);

			drawLives(g);
			drawLevelCounter(g);
		}
	}

	protected void drawLives(Graphics2D g) {
		g.translate(0, size.height - 2 * TS);
		for (int i = 0; i < game.pacMan.lives; ++i) {
			g.translate((2 - i) * lifeImage.getWidth(null), 0);
			g.drawImage(lifeImage, 0, 0, null);
			g.translate((i - 2) * lifeImage.getWidth(null), 0);
		}
		g.translate(0, -size.height + 2 * TS);
	}

	protected void drawLevelCounter(Graphics2D g) {
		int mazeWidth = mazeView.sprites.current().get().getWidth();
		g.translate(0, size.height - 2 * TS);
		for (int i = 0, n = game.getLevelCounter().size(); i < n; ++i) {
			g.translate(mazeWidth - (n - i + 1) * 2 * TS, 0);
			Image bonusImage = theme.spr_bonusSymbol(game.getLevelCounter().get(i)).frame(0);
			g.drawImage(bonusImage, 0, 0, 2 * TS, 2 * TS, null);
			g.translate(-mazeWidth + (n - i + 1) * 2 * TS, 0);
		}
		g.translate(0, -size.height + 2 * TS);
	}

	protected void drawInfoText(Graphics2D g) {
		if (infoText == null) {
			return;
		}
		int mazeWidth = mazeView.sprites.current().get().getWidth();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(theme.fnt_text(14));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((mazeWidth - box.width) / 2, (game.maze.bonusTile.row + 1) * TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}
}