package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.view.core.EntityRenderer.drawEntity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Symbol;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements LivingView {

	protected World world;
	protected Theme theme;
	protected Game game;
	protected int width;
	protected int height;

	private boolean mazeEmpty;
	private boolean mazeFlashing;
	private final Sprite mazeEmptySprite, mazeFullSprite, mazeFlashingSprite;
	private final SpriteAnimation energizerAnimation;
	private int offsetY;

	private String messageText = "";
	private Color messageColor = Color.YELLOW;
	private int messageFontSize = 8;
	private int messageRow = 21;

	public SimplePlayView(World world, Theme theme, Game game, int width, int height) {
		this.world = world;
		this.theme = theme;
		this.game = game;
		this.width = width;
		this.height = height;
		mazeFullSprite = theme.spr_fullMaze();
		mazeEmptySprite = theme.spr_emptyMaze();
		mazeFlashingSprite = theme.spr_flashingMaze();
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
		energizerAnimation.setEnabled(false);
		offsetY = 3 * Tile.SIZE;
	}

	@Override
	public void init() {
		clearMessage();
		setEmptyMaze(false);
		setMazeFlashing(false);
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		if (game != null) {
			drawScores(g, game);
		}
		drawMaze(g);
		drawMessage(g);
		drawActors(g);
	}

	public void showMessage(String text, Color color, int fontSize) {
		messageText = text;
		messageColor = color;
		messageFontSize = fontSize;
	}

	public void showMessage(String text, Color color) {
		messageText = text;
		messageColor = color;
		messageFontSize = 8;
	}

	public void clearMessage() {
		messageText = "";
	}

	public void enableGhostAnimations(boolean enabled) {
		world.population().ghosts().flatMap(ghost -> ghost.sprites.values())
				.forEach(sprite -> sprite.enableAnimation(enabled));
	}

	public void enableEnergizerAnimations(boolean enabled) {
		energizerAnimation.setEnabled(enabled);
	}

	protected Color tileColor(Tile tile) {
		return Color.BLACK;
	}

	public void setMazeFlashing(boolean flashing) {
		mazeFlashing = flashing;
	}

	public void setEmptyMaze(boolean empty) {
		mazeEmpty = empty;
	}

	protected void drawMaze(Graphics2D g) {
		if (mazeEmpty) {
			if (mazeFlashing) {
				drawFlashingEmptyMaze(g);
			} else {
				drawEmptyMaze(g);
			}
		} else {
			drawNormalMaze(g);
			energizerAnimation.update();
		}
	}

	public void drawNormalMaze(Graphics2D g) {
		g.translate(0, offsetY);
		mazeFullSprite.draw(g);
		g.translate(0, -offsetY);

		// hide eaten food
		world.habitatTiles().filter(world::containsEatenFood).forEach(tile -> {
			g.setColor(tileColor(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.habitatTiles().filter(world::containsEnergizer).forEach(tile -> {
				g.setColor(tileColor(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus when active or consumed
		world.getBonus().filter(bonus -> bonus.state != BonusState.INACTIVE).ifPresent(bonus -> {
			Sprite sprite = bonus.state == BonusState.CONSUMED ? theme.spr_number(bonus.value)
					: theme.spr_bonusSymbol(bonus.symbol);
			g.drawImage(sprite.frame(0), world.bonusTile().x(), world.bonusTile().y() - Tile.SIZE / 2, null);
		});
		// draw doors depending on their state
		world.theHouse().doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
			g.setColor(Color.BLACK);
			door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
		});
	}

	public void drawFlashingEmptyMaze(Graphics2D g) {
		g.translate(0, offsetY);
		mazeFlashingSprite.draw(g);
		g.translate(0, -offsetY);
	}

	public void drawEmptyMaze(Graphics2D g) {
		g.translate(0, offsetY);
		mazeEmptySprite.draw(g);
		g.translate(0, -offsetY);
	}

	protected void drawMessage(Graphics2D g) {
		int width = world.width() * Tile.SIZE;
		if (messageText != null && messageText.trim().length() > 0) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text());
				pen.fontSize(messageFontSize);
				pen.color(messageColor);
				pen.hcenter(messageText, width, messageRow, Tile.SIZE);
			}
		}
	}

	protected void drawActors(Graphics2D g) {
		drawEntity(g, world.population().pacMan(), world.population().pacMan().sprites);
		// draw dead ghosts (as number or eyes) under living ghosts
		world.population().ghosts().filter(world::included).filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> drawEntity(g, ghost, ghost.sprites));
		world.population().ghosts().filter(world::included).filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> drawEntity(g, ghost, ghost.sprites));
	}

	protected void drawScores(Graphics2D g, Game game) {
		int topMargin = 3;
		int lineOffset = 2;
		Color hilight = Color.YELLOW;
		int col;
		g.translate(0, topMargin); // margin between score and upper window border
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());

			// Game score
			col = 1;
			pen.color(hilight);
			pen.drawAtGridPosition("Score".toUpperCase(), col, 0, Tile.SIZE);

			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%7d", game.score), col, 1, Tile.SIZE);
			pen.up(lineOffset);

			// Highscore
			col = 9;
			pen.color(hilight);
			pen.drawAtGridPosition("High Score".toUpperCase(), col, 0, Tile.SIZE);
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%7d", game.hiscore.points), col, 1, Tile.SIZE);
			pen.color(Color.LIGHT_GRAY);
			pen.drawAtGridPosition(String.format("L%02d", game.hiscore.level), col + 7, 1, Tile.SIZE);
			pen.up(lineOffset);

			col = 21;
			pen.color(hilight);
			pen.drawAtGridPosition(String.format("Level".toUpperCase()), col, 0, Tile.SIZE);
			// Level number
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%02d", game.level.number), col, 1, Tile.SIZE);
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.translate(0, (topMargin + lineOffset) - 2);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.fillOval((col + 2) * Tile.SIZE + 2, Tile.SIZE, 4, 4); // dot image
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.translate(0, -(topMargin + lineOffset) + 2);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%03d", game.level.remainingFoodCount()), col + 3, 1, Tile.SIZE);
			pen.up(lineOffset);
		}
		g.translate(0, -topMargin);

		drawLives(g, game);
		drawLevelCounter(g, game);
	}

	protected void drawLives(Graphics2D g, Game game) {
		int height = world.height() * Tile.SIZE;
		int sz = 2 * Tile.SIZE;
		Image pacManLookingLeft = theme.spr_pacManWalking(LEFT).frame(1);
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.drawImage(pacManLookingLeft, x, height - sz, null);
		}
	}

	protected void drawLevelCounter(Graphics2D g, Game game) {
		int max = 7;
		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int sz = 2 * Tile.SIZE; // image size
		int width = world.width() * Tile.SIZE;
		int height = world.height() * Tile.SIZE;
		for (int i = 0, x = width - 2 * sz; i < n; ++i, x -= sz) {
			Symbol symbol = game.levelCounter.get(first + i);
			g.drawImage(theme.spr_bonusSymbol(symbol.name()).frame(0), x, height - sz, sz, sz, null);
		}
	}
}