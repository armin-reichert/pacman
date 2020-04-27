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
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.PacManGameView;
import de.amr.games.pacman.view.core.Pen;

/**
 * Simple play view providing core functionality.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView extends PacManGameView {

	enum Mode {
		EMPTY_MAZE, CROWDED_MAZE, FLASHING_MAZE
	}

	protected final Game game;
	protected final Theme theme;
	protected Mode mode;
	protected SpriteAnimation energizerBlinking;
	protected Image imageLife;
	protected Sprite spriteMazeEmpty;
	protected Sprite spriteMazeFull;
	protected Sprite spriteMazeFlashing;
	protected String messageText;
	protected Color messageColor;

	public BooleanSupplier showScores = () -> true;

	public SimplePlayView(Game game, Theme theme) {
		this.game = game;
		this.theme = theme;
		mode = Mode.CROWDED_MAZE;
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(150);
		imageLife = theme.spr_pacManWalking(3).frame(1);
		spriteMazeFull = theme.spr_fullMaze();
		spriteMazeEmpty = theme.spr_emptyMaze();
		spriteMazeFlashing = theme.spr_flashingMaze();
		messageText = null;
		messageColor = Color.YELLOW;
		dress(theme, game);
		game.bonus.tf.setPosition(game.maze.bonusTile.centerX(), game.maze.bonusTile.y());
	}

	@Override
	public void init() {
		stopEnergizerBlinking();
		clearMessage();
		showCrowdedMaze();
	}

	@Override
	public void update() {
		if (mode == Mode.CROWDED_MAZE) {
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
		spriteMazeFlashing.enableAnimation(true);
		game.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(true));
	}

	public void disableAnimations() {
		spriteMazeFlashing.enableAnimation(false);
		game.ghostsOnStage().forEach(ghost -> ghost.enableAnimations(false));
	}

	public float mazeFlashingSeconds() {
		return game.level().mazeNumFlashes * Theme.MAZE_FLASH_TIME_MILLIS / 1000f;
	}

	public void showEmptyMaze() {
		mode = Mode.EMPTY_MAZE;
	}

	public void showFlashingMaze() {
		mode = Mode.FLASHING_MAZE;
	}

	public void showCrowdedMaze() {
		mode = Mode.CROWDED_MAZE;
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
		return theme.color_mazeBackground();
	}

	protected void fillBackground(Graphics2D g) {
		g.setColor(theme.color_mazeBackground());
		g.fillRect(0, 0, width(), height());
	}

	protected void drawMaze(Graphics2D g) {
		switch (mode) {
		case CROWDED_MAZE:
			drawCrowdedMaze(g);
			break;
		case EMPTY_MAZE:
			drawEmptyMaze(g);
			break;
		case FLASHING_MAZE:
			drawFlashingMaze(g);
			break;
		default:
			break;
		}
	}

	protected void drawCrowdedMaze(Graphics2D g) {
		spriteMazeFull.draw(g, 0, 3 * Tile.SIZE);
		game.maze.tiles().filter(Tile::containsEatenFood).forEach(tile -> {
			g.setColor(bgColor(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// hide active energizers when blinking animation is in dark phase
		if (energizerBlinking.currentFrame() == 1) {
			Arrays.stream(game.maze.energizers).filter(tile -> !tile.containsEatenEnergizer()).forEach(tile -> {
				g.setColor(bgColor(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// hide door when ghost is passing through
		if (game.ghostsOnStage().anyMatch(ghost -> game.maze.isDoor(ghost.tile()))) {
			g.setColor(theme.color_mazeBackground());
			g.fillRect(game.maze.doorLeft.x(), game.maze.doorLeft.y(), 2 * Tile.SIZE, Tile.SIZE);
		}
	}

	protected void drawEmptyMaze(Graphics2D g) {
		spriteMazeEmpty.draw(g, 0, 3 * Tile.SIZE);
	}

	protected void drawFlashingMaze(Graphics2D g) {
		spriteMazeFlashing.draw(g, 0, 3 * Tile.SIZE);
	}

	protected void drawActors(Graphics2D g) {
		drawBonus(game.bonus, g);
		drawPacMan(game.pacMan, g);
		// draw dead ghosts (points) under living ghosts
		game.ghostsOnStage().filter(ghost -> ghost.is(DEAD)).forEach(ghost -> drawGhost(ghost, g));
		game.ghostsOnStage().filter(ghost -> !ghost.is(DEAD)).forEach(ghost -> drawGhost(ghost, g));
	}

	protected void drawScores(Graphics2D g) {
		if (!showScores.getAsBoolean()) {
			return;
		}
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text(10));
			// Game score
			pen.color(Color.YELLOW);
			pen.drawAtTilePosition(1, 0, "SCORE");
			pen.drawAtTilePosition(22, 0, String.format("LEVEL%2d", game.level().number));
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(1, 1, String.format("%07d", game.score));
			// Highscore
			pen.color(Color.YELLOW);
			pen.drawAtTilePosition(10, 0, "HIGHSCORE");
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(10, 1, String.format("%07d", game.hiscore().points));
			pen.drawAtTilePosition(16, 1, String.format("L%d", game.hiscore().levelNumber));
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.fillRect(22 * Tile.SIZE + 2, Tile.SIZE + 2, 4, 3);
			pen.color(Color.WHITE);
			pen.drawAtTilePosition(23, 1, String.format("%03d", game.numPelletsRemaining()));
		}
		drawLives(g);
		drawLevelCounter(g);
	}

	protected void drawLives(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		for (int i = 0, x = imageSize; i < game.lives; ++i, x += imageSize) {
			g.drawImage(imageLife, x, height() - imageSize, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g) {
		int imageSize = 2 * Tile.SIZE;
		int x = width() - (game.levelCounter().size() + 1) * imageSize;
		for (Symbol symbol : game.levelCounter()) {
			Image image = theme.spr_bonusSymbol(symbol).frame(0);
			g.drawImage(image, x, height() - imageSize, imageSize, imageSize, null);
			x += imageSize;
		}
	}

	protected void drawMessage(Graphics2D g) {
		if (messageText != null && messageText.trim().length() > 0) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text(11));
				pen.color(messageColor);
				pen.hcenter(messageText, width(), 21);
			}
		}
	}
}