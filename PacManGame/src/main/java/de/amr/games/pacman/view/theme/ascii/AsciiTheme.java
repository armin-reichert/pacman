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
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

public class AsciiTheme implements Theme {

	static final Font font = new Font(Font.MONOSPACED, Font.BOLD, Tile.SIZE);
	static final int offsetY = Tile.SIZE - 1;

	@Override
	public String name() {
		return "ASCII";
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return g -> {
			if (!ghost.visible) {
				return;
			}
			g.translate(0, offsetY);
			g.setFont(font);
			Color color = Color.GREEN;
			switch (ghost.name) {
			case "Blinky":
				color = Color.RED;
				break;
			case "Pinky":
				color = Color.PINK;
				break;
			case "Inky":
				color = Color.CYAN;
				break;
			case "Clyde":
				color = Color.ORANGE;
				break;
			default:
			}
			String ghostChar;
			switch (ghost.getState()) {
			case FRIGHTENED:
				ghostChar = ghost.name.substring(0, 1).toLowerCase();
				break;
			case DEAD:
			case ENTERING_HOUSE:
				ghostChar = Rendering.INFTY;
				break;
			default:
				ghostChar = ghost.name.substring(0, 1);
			}
			g.setColor(color);
			g.drawString(ghostChar, ghost.tf.x, ghost.tf.y);
			g.translate(0, -offsetY);
		};
	}

	@Override
	public IRenderer createPacManRenderer(World world) {
		return g -> {
			PacMan pacMan = world.population().pacMan();
			if (!pacMan.visible) {
				return;
			}
			g.translate(0, offsetY);
			g.setFont(font);
			g.setColor(Color.YELLOW);
			g.drawString("O", pacMan.tf.x, pacMan.tf.y);
			g.translate(0, -offsetY);
		};
	}

	@Override
	public IRenderer createLevelCounterRenderer(World world, Game game) {
		return g -> {
			g.translate(0, offsetY);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(String.format("Level: %d", game.level.number), -8 * Tile.SIZE, -Tile.SIZE);
			g.translate(0, -offsetY);
		};
	}

	@Override
	public IRenderer createLiveCounterRenderer(World world, Game game) {
		return g -> {
			g.translate(0, offsetY);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(String.format("Lives: %d", game.lives), Tile.SIZE, -Tile.SIZE);
			g.translate(0, -offsetY);
		};
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		return g -> {
			g.translate(0, offsetY);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(" Score          Highscore        Pellets", 0, 0);
			g.drawString(String.format(" %08d       %08d         %03d", game.score, game.hiscore.points,
					game.level.remainingFoodCount()), 0, Tile.SIZE);
			g.translate(0, -offsetY);
		};
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return g -> {
			g.translate(0, offsetY);
			g.setFont(font);
			for (int row = 3; row < world.height() - 2; ++row) {
				for (int col = 0; col < world.width(); ++col) {
					Tile tile = Tile.at(col, row);
					if (world.isAccessible(tile)) {
						if (world.containsEnergizer(tile) && Application.app().clock().getTotalTicks() % 60 < 30) {
							g.setColor(Color.PINK);
							g.drawString("Ö", col * Tile.SIZE, row * Tile.SIZE);
						}
						if (world.containsSimplePellet(tile)) {
							g.setColor(Color.PINK);
							g.drawString(".", col * Tile.SIZE, row * Tile.SIZE - 2);
						}
					} else {
						g.setColor(Rendering.alpha(Color.GREEN, 80));
						g.drawString("#", col * Tile.SIZE, row * Tile.SIZE);
					}
				}
			}
			world.houses().flatMap(House::doors).forEach(door -> {
				if (door.state == DoorState.CLOSED) {
					g.setColor(Color.PINK);
					door.tiles.forEach(tile -> {
						g.fillRect(tile.x(), tile.y() - Tile.SIZE + 3, Tile.SIZE - 1, 2);
					});
				}
			});
			g.translate(0, -offsetY);
		};
	}

	@Override
	public MessagesRenderer createMessagesRenderer() {
		MessagesRenderer renderer = new MessagesRenderer();
		renderer.setFont(font);
		return renderer;
	}
}