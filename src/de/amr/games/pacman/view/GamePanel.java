package de.amr.games.pacman.view;

import static de.amr.games.pacman.model.Game.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.GhostState;
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
		mazePanel = new MazePanel(game.getMaze(), actors);
		mazePanel.tf.moveTo(0, 3 * TS);
		Assets.storeTrueTypeFont("scoreFont", "arcadeclassic.ttf", Font.PLAIN, TS * 3 / 2);
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
		actors.getPacMan().enableAnimation(enable);
		actors.getActiveGhosts().forEach(ghost -> ghost.enableAnimation(enable));
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
		drawScores(g);
		drawLives(g);
		drawLevelCounter(g);
		mazePanel.draw(g);
		drawActors(g);
		drawInfoText(g);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(0, getHeight() - 2 * TS);
		for (int i = 0, n = game.getLevelCounter().size(); i < n; ++i) {
			g.translate(getWidth() - (n - i) * 2 * TS, 0);
			g.drawImage(SPRITES.symbolImage(game.getLevelCounter().get(i)), 0, 0, 2 * TS, 2 * TS, null);
			g.translate(-getWidth() + (n - i) * 2 * TS, 0);
		}
		g.translate(0, -getHeight() + 2 * TS);
	}

	protected void drawLives(Graphics2D g) {
		g.translate(0, getHeight() - 2 * TS);
		for (int i = 0; i < game.getLives(); ++i) {
			g.translate((2 - i) * lifeImage.getWidth(null), 0);
			g.drawImage(lifeImage, 0, 0, null);
			g.translate((i - 2) * lifeImage.getWidth(null), 0);
		}
		g.translate(0, -getHeight() + 2 * TS);
	}

	protected void drawScores(Graphics2D g) {
		// Score
		int score = game.score.getScore();
		g.setFont(Assets.font("scoreFont"));
		g.setColor(Color.YELLOW);
		g.drawString("SCORE", TS, TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", score), TS, 2 * TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("LEVEL %2d", game.getLevel()), 22 * TS, TS);

		// Highscore
		g.setColor(Color.YELLOW);
		g.drawString("HIGH", 10 * TS, TS);
		g.drawString("SCORE", 14 * TS, TS);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%07d", game.score.getHiscore()), 10 * TS, 2 * TS);
		g.drawString(String.format("L%d", game.score.getHiscoreLevel()), 16 * TS, 2 * TS);

		// Food remaining
		g.setColor(Color.PINK);
		g.fillRect(22 * TS + 2, TS + 2, 4, 4);
		g.setColor(Color.WHITE);
		g.drawString(String.format("%d", game.getFoodRemaining()), 23 * TS, 2 * TS);
	}

	protected void drawActors(Graphics2D g) {
		actors.getBonus().ifPresent(bonus -> {
			bonus.placeAtTile(game.getMaze().getBonusTile(), TS / 2, 0);
			bonus.draw(g);
		});
		actors.getPacMan().draw(g);
		actors.getActiveGhosts().filter(ghost -> ghost.getState() != GhostState.DYING)
				.forEach(ghost -> ghost.draw(g));
		actors.getActiveGhosts().filter(ghost -> ghost.getState() == GhostState.DYING)
				.forEach(ghost -> ghost.draw(g));
	}

	protected void drawInfoText(Graphics2D g) {
		if (infoText == null) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(Assets.font("scoreFont"));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((width - box.width) / 2, (game.getMaze().getBonusTile().row + 1) * TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}

}