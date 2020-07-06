package de.amr.games.pacman.view.theme.ascii;

import java.awt.Color;
import java.awt.Font;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

public class AsciiTheme implements Theme {

	static final Font font = new Font("Courier New", Font.PLAIN, Tile.SIZE);

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
		};
	}

	@Override
	public IRenderer createPacManRenderer(World world) {
		return g -> {
			PacMan pacMan = world.population().pacMan();
			if (!pacMan.visible) {
				return;
			}
			g.setFont(font);
			g.setColor(Color.YELLOW);
			g.drawString("O", pacMan.tf.x, pacMan.tf.y);
		};
	}

	@Override
	public IRenderer createLevelCounterRenderer(Game game) {
		return g -> {

		};
	}

	@Override
	public IRenderer createLiveCounterRenderer(Game game) {
		return g -> {

		};
	}

	@Override
	public IRenderer createScoreRenderer(Game game) {
		return g -> {

		};
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return g -> {
			g.setFont(font);
			for (int row = 0; row < world.height(); ++row) {
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
						g.setColor(Color.GREEN);
						g.drawString("#", col * Tile.SIZE, row * Tile.SIZE);
					}
				}
			}
		};
	}

	@Override
	public MessagesRenderer createMessagesRenderer() {
		return new MessagesRenderer();
	}
}