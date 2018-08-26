package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.Game.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.view.ViewController;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManWorld;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.PacManThemes;

/**
 * Simple play view without bells and whistles.
 * 
 * @author Armin Reichert
 */
public class PlayView implements ViewController, PacManWorld {

	protected final int width, height;
	protected final Game game;
	protected final MazeView mazeView;
	protected final Image lifeImage;
	protected Cast actors;
	protected String infoText;
	protected Color infoTextColor;
	protected boolean scoresVisible;

	public PlayView(int width, int height, Game game) {
		this.width = width;
		this.height = height;
		this.game = game;
		lifeImage = PacManThemes.THEME.pacManWalking(Top4.W).frame(1);
		mazeView = new MazeView(game.getMaze());
		mazeView.tf.moveTo(0, 3 * TS);
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
		mazeView.init();
	}

	@Override
	public void update() {
		mazeView.update();
	}

	public void enableAnimation(boolean enable) {
		mazeView.enableAnimation(enable);
		actors.getPacMan().enableAnimation(enable);
		actors.getActiveGhosts().forEach(ghost -> ghost.enableAnimation(enable));
	}

	public void setActors(Cast actors) {
		this.actors = actors;
	}

	@Override
	public Stream<Ghost> getActiveGhosts() {
		return actors.getActiveGhosts();
	}

	@Override
	public Ghost getBlinky() {
		return actors.getBlinky();
	}

	@Override
	public Ghost getClyde() {
		return actors.getClyde();
	}

	@Override
	public Ghost getInky() {
		return actors.getInky();
	}

	@Override
	public Ghost getPinky() {
		return actors.getPinky();
	}

	@Override
	public PacMan getPacMan() {
		return actors.getPacMan();
	}

	@Override
	public Optional<Bonus> getBonus() {
		return mazeView.getBonus();
	}

	public void setBonusTimer(int ticks) {
		mazeView.setBonusTimer(ticks);
	}

	public void setBonus(BonusSymbol symbol, int value) {
		mazeView.setBonus(new Bonus(symbol, value));
	}

	public void removeBonus() {
		mazeView.setBonus(null);
	}

	public void setMazeFlashing(boolean flashing) {
		mazeView.setFlashing(flashing);
	}

	public void showInfoText(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void hideInfoText() {
		this.infoText = null;
	}

	public boolean isScoresVisible() {
		return scoresVisible;
	}

	public void setScoresVisible(boolean scoresVisible) {
		this.scoresVisible = scoresVisible;
	}

	@Override
	public void draw(Graphics2D g) {
		mazeView.draw(g);
		drawActors(g);
		drawInfoText(g);
		drawScores(g);
	}

	protected void drawActors(Graphics2D g) {
		actors.getPacMan().draw(g);
		actors.getActiveGhosts().filter(ghost -> ghost.getState() != GhostState.DYING)
				.forEach(ghost -> ghost.draw(g));
		actors.getActiveGhosts().filter(ghost -> ghost.getState() == GhostState.DYING)
				.forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (scoresVisible) {
			// Points score
			int score = game.score.getScore();
			g.setFont(PacManThemes.THEME.textFont());
			g.setColor(Color.YELLOW);
			g.drawString("SCORE", TS, TS);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%07d", score), TS, 2 * TS);
			g.setColor(Color.YELLOW);
			g.drawString(String.format("LEVEL %2d", game.getLevel()), 22 * TS, TS);

			// High score
			g.setColor(Color.YELLOW);
			g.drawString("HIGH", 10 * TS, TS);
			g.drawString("SCORE", 14 * TS, TS);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%07d", game.score.getHiscore()), 10 * TS, 2 * TS);
			g.drawString(String.format("L%d", game.score.getHiscoreLevel()), 16 * TS, 2 * TS);

			// Food remaining score
			g.setColor(Color.PINK);
			g.fillRect(22 * TS + 2, TS + 2, 4, 4);
			g.setColor(Color.WHITE);
			g.drawString(String.format("%d", game.getFoodRemaining()), 23 * TS, 2 * TS);

			drawLives(g);
			drawLevelCounter(g);
		}
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

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(0, getHeight() - 2 * TS);
		for (int i = 0, n = game.getLevelCounter().size(); i < n; ++i) {
			g.translate(getWidth() - (n - i) * 2 * TS, 0);
			g.drawImage(PacManThemes.THEME.symbolImage(game.getLevelCounter().get(i)), 0, 0, 2 * TS,
					2 * TS, null);
			g.translate(-getWidth() + (n - i) * 2 * TS, 0);
		}
		g.translate(0, -getHeight() + 2 * TS);
	}

	protected void drawInfoText(Graphics2D g) {
		if (infoText == null) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(PacManThemes.THEME.textFont());
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((width - box.width) / 2, (game.getMaze().getBonusTile().row + 1) * TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}
}