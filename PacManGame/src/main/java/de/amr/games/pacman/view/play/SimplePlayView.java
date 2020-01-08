package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.actor.GhostState.DEAD;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.GameView;
import de.amr.games.pacman.view.core.Pen;

/**
 * Simple play view providing core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements GameView {

	protected final Cast cast;
	protected boolean mazeFlashing;
	protected SpriteAnimation energizerBlinking;
	protected Image lifeImage;
	protected Sprite fullMazeSprite;
	protected Sprite flashingMazeSprite;
	protected String messageText;
	protected Color messageColor;

	public BooleanSupplier showScores = () -> true;

	public SimplePlayView(Cast cast) {
		this.cast = cast;
		mazeFlashing = false;
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		cast.addThemeListener(this);
		onThemeChanged(theme());
		messageText = null;
		messageColor = Color.YELLOW;
	}

	@Override
	public boolean visible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	public void onThemeChanged(Theme theme) {
		lifeImage = theme.spr_pacManWalking(3).frame(1);
		fullMazeSprite = theme.spr_fullMaze();
		flashingMazeSprite = theme.spr_flashingMaze();
	}

	public Cast cast() {
		return cast;
	}

	@Override
	public Theme theme() {
		return cast.theme();
	}

	public Game game() {
		return cast.game();
	}

	public Maze maze() {
		return game().maze();
	}

	@Override
	public void init() {
		stopEnergizerBlinking();
		stopMazeFlashing();
		clearMessage();
	}

	@Override
	public void update() {
		if (!mazeFlashing) {
			energizerBlinking.update();
		}
	}

	public void messageColor(Color color) {
		this.messageColor = color;
	}

	public void message(String message) {
		this.messageText = message;
	}

	public void clearMessage() {
		messageText = null;
	}

	public void enableAnimations() {
		flashingMazeSprite.enableAnimation(true);
		cast.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(true));
	}

	public void disableAnimations() {
		flashingMazeSprite.enableAnimation(false);
		cast.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(false));
	}

	public int mazeFlashingSeconds() {
		return game().level().mazeNumFlashes * Theme.MAZE_FLASH_TIME_MILLIS / 1000;
	}

	public void startMazeFlashing() {
		mazeFlashing = true;
	}

	public void stopMazeFlashing() {
		mazeFlashing = false;
	}

	public void startEnergizerBlinking() {
		energizerBlinking.setEnabled(true);
	}

	public void stopEnergizerBlinking() {
		energizerBlinking.setEnabled(false);
	}

	@Override
	public void draw(Graphics2D g) {
		fillBackground(g);
		drawScores(g);
		drawMaze(g);
		drawMessage(g);
		drawActors(g);
	}

	protected Color bgColor(Tile tile) {
		return theme().color_mazeBackground();
	}

	protected void fillBackground(Graphics2D g) {
		g.setColor(theme().color_mazeBackground());
		g.fillRect(0, 0, width(), height());
	}

	protected void drawMaze(Graphics2D g) {
		if (mazeFlashing) {
			flashingMazeSprite.draw(g, 0, 3 * Tile.SIZE);
		} else {
			fullMazeSprite.draw(g, 0, 3 * Tile.SIZE);
			maze().tiles().filter(Tile::containsEatenFood).forEach(tile -> {
				g.setColor(bgColor(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
			// hide energizer tiles when blinking animation is dark
			if (energizerBlinking.currentFrame() == 1) {
				Arrays.stream(maze().energizers).forEach(tile -> {
					g.setColor(bgColor(tile));
					g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
				});
			}
			// hide door when ghost is passing through
			if (cast.ghostsOnStage().anyMatch(ghost -> maze().isDoor(ghost.tile()))) {
				g.setColor(theme().color_mazeBackground());
				g.fillRect(maze().doorLeft.x(), maze().doorLeft.y(), 2 * Tile.SIZE, Tile.SIZE);
			}
		}
	}

	protected void drawActors(Graphics2D g) {
		cast.bonus().ifPresent(bonus -> bonus.draw(g));
		cast.pacMan.draw(g);
		// draw dead ghosts (numbers) under living ghosts
		cast.ghostsOnStage().filter(ghost -> ghost.is(DEAD)).forEach(ghost -> ghost.draw(g));
		cast.ghostsOnStage().filter(ghost -> !ghost.is(DEAD)).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores.getAsBoolean()) {
			return;
		}
		try (Pen pen = new Pen(g)) {
			pen.font(theme().fnt_text(10));
			// Game score
			pen.color(Color.YELLOW);
			pen.drawAtTilePosition(1, 0, "SCORE");
			pen.drawAtTilePosition(22, 0, String.format("LEVEL%2d", game().level().number));
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(1, 1, String.format("%07d", game().score));
			// Highscore
			pen.color(Color.YELLOW);
			pen.drawAtTilePosition(10, 0, "HIGHSCORE");
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(10, 1, String.format("%07d", game().hiscore().points));
			pen.drawAtTilePosition(16, 1, String.format("L%d", game().hiscore().levelNumber));
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.fillRect(22 * Tile.SIZE + 2, Tile.SIZE + 2, 4, 3);
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(23, 1, String.format("%03d", game().numPelletsRemaining()));
		}
		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		for (int i = 0, x = imageSize; i < game().lives; ++i, x += imageSize) {
			g.drawImage(lifeImage, x, height() - imageSize, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		int x = width() - (game().levelCounter().size() + 1) * imageSize;
		for (Symbol symbol : game().levelCounter()) {
			Image image = theme().spr_bonusSymbol(symbol).frame(0);
			g.drawImage(image, x, height() - imageSize, imageSize, imageSize, null);
			x += imageSize;
		}
	}

	protected void drawMessage(Graphics2D g) {
		if (messageText != null && messageText.trim().length() > 0) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme().fnt_text(11));
				pen.color(messageColor);
				pen.hcenter(messageText, width(), 21);
			}
		}
	}
}