package de.amr.games.pacman.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;

public class BasicGamePanel implements PacManGameUI {

	protected final int width, height;
	protected final Game game;
	protected final Cast actors;
	protected final MazePanel mazeUI;
	protected final Font font;
	protected final Image lifeImage;
	protected String infoText;
	protected Color infoTextColor;

	public BasicGamePanel(int width, int height, Game game, Cast actors) {
		this.width = width;
		this.height = height;
		this.game = game;
		this.actors = actors;
		font = Assets.storeTrueTypeFont("scoreFont", "arcadeclassic.ttf", Font.PLAIN, PacManGameUI.TS * 3 / 2);
		lifeImage = PacManGameUI.SPRITES.pacManWalking(Top4.W).frame(1);
		mazeUI = new MazePanel(game.maze, actors);
		mazeUI.tf.moveTo(0, 3 * PacManGameUI.TS);
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
		mazeUI.update();
	}

	@Override
	public View currentView() {
		return this;
	}

	public void enableAnimation(boolean enable) {
		mazeUI.enableAnimation(enable);
	}

	public void setBonusTimer(int ticks) {
		mazeUI.setBonusTimer(ticks);
	}

	public void setMazeFlashing(boolean flashing) {
		mazeUI.setFlashing(flashing);
	}

	public void showInfo(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void hideInfo() {
		this.infoText = null;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(font);
		g.drawString("SCORE", PacManGameUI.TS, PacManGameUI.TS);
		g.drawString(String.format("%06d", game.score), PacManGameUI.TS, PacManGameUI.TS * 2);
		g.drawString("LEVEL " + game.level, 20 * PacManGameUI.TS, PacManGameUI.TS);
		g.translate(0, getHeight() - 2 * PacManGameUI.TS);
		for (int i = 0; i < game.livesRemaining; ++i) {
			g.translate(i * lifeImage.getWidth(null), 0);
			g.drawImage(lifeImage, 0, 0, null);
			g.translate(-i * lifeImage.getWidth(null), 0);
		}
		g.translate(0, -getHeight() + 2 * PacManGameUI.TS);
		mazeUI.draw(g);
		if (infoText != null) {
			drawInfoText(g);
		}
	}

	private void drawInfoText(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
		g2.setFont(Assets.font("scoreFont"));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((width - box.width) / 2, (game.maze.infoTile.row + 1) * PacManGameUI.TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}

}