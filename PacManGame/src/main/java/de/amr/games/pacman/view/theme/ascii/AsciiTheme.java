package de.amr.games.pacman.view.theme.ascii;

import java.awt.Color;
import java.awt.Font;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.IPacManRenderer;
import de.amr.games.pacman.view.core.IRenderer;
import de.amr.games.pacman.view.core.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

/**
 * Theme using ASCII characters only (almost).
 * 
 * @author Armin Reichert
 */
public class AsciiTheme implements Theme {

	public static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, Tile.SIZE);
	public static final int OFFSET_BASELINE = Tile.SIZE - 1;

	public static Color ghostColor(Ghost ghost) {
		switch (ghost.name) {
		case "Blinky":
			return Color.RED;
		case "Pinky":
			return Color.PINK;
		case "Inky":
			return Color.CYAN;
		case "Clyde":
			return Color.ORANGE;
		default:
			return Color.WHITE;
		}
	}

	public static String ghostLetter(Ghost ghost) {
		if (ghost.getState() == null) {
			return ghost.name.substring(0, 1);
		}
		switch (ghost.getState()) {
		case FRIGHTENED:
			return ghost.name.substring(0, 1).toLowerCase();
		case DEAD:
		case ENTERING_HOUSE:
			return Rendering.INFTY;
		default:
			return ghost.name.substring(0, 1);
		}
	}

	@Override
	public String name() {
		return "ASCII";
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return g -> {
			if (ghost.visible) {
				g.setFont(FONT);
				g.setColor(ghostColor(ghost));
				g.drawString(ghostLetter(ghost), ghost.tf.x, ghost.tf.y + OFFSET_BASELINE);
			}
		};
	}

	@Override
	public IPacManRenderer createPacManRenderer(PacMan pacMan) {
		return g -> {
			if (pacMan.visible) {
				g.setFont(FONT);
				g.setColor(Color.YELLOW);
				g.drawString("O", pacMan.tf.x, pacMan.tf.y + OFFSET_BASELINE);
			}
		};
	}

	@Override
	public IRenderer createLevelCounterRenderer(World world, Game game) {
		return g -> {
			String text = String.format("Level: %d (%s)", game.level.number, game.level.bonusSymbol);
			g.setColor(Color.YELLOW);
			g.setFont(FONT);
			g.drawString(text, -15 * Tile.SIZE, Tile.SIZE + OFFSET_BASELINE);
		};
	}

	@Override
	public IRenderer createLiveCounterRenderer(World world, Game game) {
		return g -> {
			g.setColor(Color.YELLOW);
			g.setFont(FONT);
			g.drawString(String.format("Lives: %d", game.lives), 0, Tile.SIZE + OFFSET_BASELINE);
		};
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		return g -> {
			g.setColor(Color.YELLOW);
			g.setFont(FONT);
			g.drawString(" Score          Highscore        Pellets", 0, OFFSET_BASELINE);
			g.drawString(String.format(" %08d       %08d         %03d", game.score, game.hiscore.points,
					game.level.remainingFoodCount()), 0, Tile.SIZE + OFFSET_BASELINE);
		};
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return g -> {
			g.setFont(FONT);
			for (int row = 3; row < world.height() - 2; ++row) {
				for (int col = 0; col < world.width(); ++col) {
					Tile tile = Tile.at(col, row);
					if (world.isAccessible(tile)) {
						if (world.containsEnergizer(tile) && Application.app().clock().getTotalTicks() % 60 < 30) {
							g.setColor(Color.PINK);
							g.drawString("Ö", col * Tile.SIZE + 2, row * Tile.SIZE + OFFSET_BASELINE);
						}
						if (world.containsSimplePellet(tile)) {
							g.setColor(Color.PINK);
							g.drawString(".", col * Tile.SIZE + 1, row * Tile.SIZE - 3 + OFFSET_BASELINE);
						}
					} else {
						g.setColor(Rendering.alpha(Color.GREEN, 80));
						g.drawString("#", col * Tile.SIZE + 1, row * Tile.SIZE + OFFSET_BASELINE - 1);
					}
				}
			}
			world.houses().flatMap(House::doors).forEach(door -> {
				if (door.state == DoorState.CLOSED) {
					g.setColor(Color.PINK);
					door.tiles.forEach(tile -> {
						g.drawString("_", tile.x() + 1, tile.y());
					});
				}
			});
		};
	}

	@Override
	public MessagesRenderer createMessagesRenderer() {
		MessagesRenderer renderer = new MessagesRenderer();
		renderer.setFont(FONT);
		return renderer;
	}
}