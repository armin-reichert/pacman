package de.amr.games.pacman.view.theme.letters;

import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Door.DoorState;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.common.AbstractTheme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

/**
 * Theme using letters only.
 * 
 * @author Armin Reichert
 */
public class LettersTheme extends AbstractTheme {

	public static final LettersTheme IT = new LettersTheme();

	private LettersTheme() {
		super("LETTERS");
		put("font", new Font(Font.MONOSPACED, Font.BOLD, Tile.SIZE));
		put("offset-baseline", Tile.SIZE - 1);
		put("ghost-colors", Map.of(
		//@formatter:off
		Ghost.RED_GHOST,    Color.RED,
		Ghost.PINK_GHOST,   Color.PINK,
		Ghost.CYAN_GHOST,   Color.CYAN,
		Ghost.ORANGE_GHOST, Color.ORANGE
		//@formatter:on
		));
	}

	private Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colors = IT.$value("ghost-colors");
		return colors.getOrDefault(ghost.getColor(), Color.WHITE);
	}

	private String ghostLetter(Ghost ghost) {
		if (ghost.getState() == null) {
			return ghost.name().substring(0, 1);
		}
		switch (ghost.getState()) {
		case FRIGHTENED:
			return ghost.name().substring(0, 1).toLowerCase();
		case DEAD:
		case ENTERING_HOUSE:
			return Rendering.INFTY;
		default:
			return ghost.name().substring(0, 1);
		}
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return g -> {
			if (ghost.isVisible()) {
				Font font = $font("font");
				int offset_baseline = $int("offset-baseline");
				g.setFont(font);
				g.setColor(ghostColor(ghost));
				if (ghost.getBounty() > 0) {
					g.drawString("" + ghost.getBounty(), ghost.entity.tf.x, ghost.entity.tf.y + offset_baseline);
				} else {
					g.drawString(ghostLetter(ghost), ghost.entity.tf.x, ghost.entity.tf.y + offset_baseline);
				}
			}
		};
	}

	@Override
	public IPacManRenderer createPacManRenderer(PacMan pacMan) {
		return g -> {
			if (pacMan.isVisible()) {
				Font font = $font("font");
				int offset_baseline = $int("offset-baseline");
				g.setFont(font);
				g.setColor(Color.YELLOW);
				String letter = pacMan.is(DEAD) ? "\u2668" : "O";
				g.drawString(letter, pacMan.entity.tf.x, pacMan.entity.tf.y + offset_baseline);
			}
		};
	}

	@Override
	public IRenderer createLevelCounterRenderer(World world, Game game) {
		return g -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			String text = String.format("Level: %d (%s)", game.level.number, game.level.bonusSymbol);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(text, -15 * Tile.SIZE, Tile.SIZE + offset_baseline);
		};
	}

	@Override
	public IRenderer createLiveCounterRenderer(World world, Game game) {
		return g -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(String.format("Lives: %d", game.lives), 0, Tile.SIZE + offset_baseline);
		};
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		return g -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(" Score          Highscore        Pellets", 0, offset_baseline);
			g.drawString(String.format(" %08d       %08d         %03d", game.score, game.hiscore.points,
					game.level.remainingFoodCount()), 0, Tile.SIZE + offset_baseline);
		};
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return g -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			g.setFont(font);
			for (int row = 3; row < world.height() - 2; ++row) {
				for (int col = 0; col < world.width(); ++col) {
					Tile tile = Tile.at(col, row);
					if (world.isAccessible(tile)) {
						if (world.containsEnergizer(tile) && Application.app().clock().getTotalTicks() % 60 < 30) {
							g.setColor(Color.PINK);
							g.drawString("Ö", col * Tile.SIZE + 2, row * Tile.SIZE + offset_baseline);
						}
						if (world.containsSimplePellet(tile)) {
							g.setColor(Color.PINK);
							g.drawString(".", col * Tile.SIZE + 1, row * Tile.SIZE - 3 + offset_baseline);
						}
					} else {
						g.setColor(Rendering.alpha(Color.GREEN, 80));
						g.drawString("#", col * Tile.SIZE + 1, row * Tile.SIZE + offset_baseline - 1);
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
		Font font = $font("font");
		renderer.setFont(font);
		return renderer;
	}
}