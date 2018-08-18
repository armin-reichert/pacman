package de.amr.games.pacman.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.game.Cast;
import de.amr.games.pacman.model.Game;

public class GamePanel implements PacManGameUI {

	protected final int width, height;
	protected final Game game;
	protected final Cast actors;
	protected final MazePanel mazePanel;
	protected final Image lifeImage;
	protected String infoText;
	protected Color infoTextColor;

	public GamePanel(int width, int height, Game game, Cast actors) {
		this.width = width;
		this.height = height;
		this.game = game;
		this.actors = actors;
		lifeImage = SPRITES.pacManWalking(Top4.W).frame(1);
		mazePanel = new MazePanel(game.maze, actors);
		mazePanel.tf.moveTo(0, 3 * Game.TS);
		Assets.storeTrueTypeFont("scoreFont", "arcadeclassic.ttf", Font.PLAIN, Game.TS * 3 / 2);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		mazePanel.update();
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void enableAnimation(boolean enable) {
		mazePanel.enableAnimation(enable);
	}

	@Override
	public void setBonusTimer(int ticks) {
		mazePanel.setBonusTimer(ticks);
	}

	@Override
	public void setMazeFlashing(boolean flashing) {
		mazePanel.setFlashing(flashing);
	}

	@Override
	public void showInfo(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	@Override
	public void hideInfo() {
		this.infoText = null;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setFont(Assets.font("scoreFont"));
		g.setColor(Color.WHITE);
		// Scores
		g.drawString("SCORE", Game.TS, Game.TS);
		g.drawString(String.format("%-6d", game.score.getScore()), Game.TS, Game.TS * 2);
		g.drawString(String.format("LEVEL %2d", game.getLevel()), 22 * Game.TS, Game.TS);
		g.drawString("HIGH", 10 * Game.TS, Game.TS);
		g.drawString("SCORE", 14 * Game.TS, Game.TS);
		g.drawString(String.format("%-6d", game.score.getHiscore()), 10 * Game.TS, Game.TS * 2);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("L%d", game.score.getHiscoreLevel()), 16 * Game.TS, Game.TS * 2);
		// Lives
		g.translate(0, getHeight() - 2 * Game.TS);
		for (int i = 0; i < game.getLives(); ++i) {
			g.translate((2 - i) * lifeImage.getWidth(null), 0);
			g.drawImage(lifeImage, 0, 0, null);
			g.translate((i - 2) * lifeImage.getWidth(null), 0);
		}
		// Level counter
		for (int i = 0, n = game.levelCounter.size(); i < n; ++i) {
			g.translate(getWidth() - (n - i) * 2 * Game.TS, 0);
			g.drawImage(SPRITES.symbolImage(game.levelCounter.get(i)), 0, 0, 2 * Game.TS, 2 * Game.TS, null);
			g.translate(-getWidth() + (n - i) * 2 * Game.TS, 0);
		}
		g.translate(0, -getHeight() + 2 * Game.TS);
		// Maze
		mazePanel.draw(g);
		// Info text
		if (infoText != null) {
			drawInfoText(g);
		}
	}

	private void drawInfoText(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(mazePanel.tf.getX(), mazePanel.tf.getY());
		g2.setFont(Assets.font("scoreFont"));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((width - box.width) / 2, (game.maze.getBonusTile().row + 1) * Game.TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}

}