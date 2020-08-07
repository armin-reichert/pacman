package de.amr.games.pacman.view.theme.letters;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.PacManSounds;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.Rendering;
import de.amr.games.pacman.view.core.AbstractTheme;
import de.amr.games.pacman.view.theme.arcade.sounds.ArcadeSounds;

/**
 * Theme using letters only.
 * 
 * @author Armin Reichert
 */
public class LettersTheme extends AbstractTheme {

	public static final LettersTheme THEME = new LettersTheme();

	private MessagesRenderer messagesRenderer;

	private LettersTheme() {
		super("LETTERS");
		put("font", new Font(Font.MONOSPACED, Font.BOLD, Tile.SIZE));
		put("offset-baseline", Tile.SIZE - 1);
		put("ghost-colors", Map.of(
		//@formatter:off
			GhostPersonality.SHADOW, Color.RED,
			GhostPersonality.SPEEDY,  Color.PINK,
			GhostPersonality.BASHFUL,   Color.CYAN,
			GhostPersonality.POKEY,  Color.ORANGE
		//@formatter:on
		));
		put("sounds", ArcadeSounds.SOUNDS);
	}

	Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colorByPersonality = $value("ghost-colors");
		return colorByPersonality.getOrDefault(ghost.getPersonality(), Color.WHITE);
	}

	String ghostLetter(Ghost ghost) {
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
	public IGhostRenderer ghostRenderer(Ghost ghost_) {
		return (g, ghost) -> {
			if (ghost.isVisible()) {
				Font font = $font("font");
				int offset_baseline = $int("offset-baseline");
				g.setFont(font.deriveFont((float) ghost.entity.tf.width));
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
	public IPacManRenderer pacManRenderer(PacMan pacMan_) {
		return (g, pacMan) -> {
			if (pacMan.isVisible()) {
				int offset_baseline = $int("offset-baseline");
				Font font = $font("font");
				g.setFont(font.deriveFont((float) pacMan.entity.tf.width));
				g.setColor(Color.YELLOW);
				String letter = pacMan.is(PacManState.DEAD) ? "\u2668" : "O";
				g.drawString(letter, pacMan.entity.tf.x, pacMan.entity.tf.y + offset_baseline);
			}
		};
	}

	@Override
	public IGameRenderer levelCounterRenderer() {
		return (g, game) -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			String text = String.format("Level: %d (%s)", game.level.number, game.level.bonusSymbol);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(text, -15 * Tile.SIZE, Tile.SIZE + offset_baseline);
		};
	}

	@Override
	public IGameRenderer livesCounterRenderer() {
		return (g, game) -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(String.format("Lives: %d", game.lives), 0, Tile.SIZE + offset_baseline);
		};
	}

	@Override
	public IGameRenderer scoreRenderer() {
		return (g, game) -> {
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
	public IWorldRenderer worldRenderer(World world_) {
		return (g, world) -> {
			Font font = $font("font");
			int offset_baseline = $int("offset-baseline");
			g.setFont(font);
			for (int row = 3; row < world.height() - 2; ++row) {
				for (int col = 0; col < world.width(); ++col) {
					Tile tile = Tile.at(col, row);
					if (world.isAccessible(tile)) {
						if (world.hasFood(Pellet.ENERGIZER, tile) && Application.app().clock().getTotalTicks() % 60 < 30) {
							g.setColor(Color.PINK);
							g.drawString("Ö", col * Tile.SIZE + 2, row * Tile.SIZE + offset_baseline);
						}
						if (world.hasFood(Pellet.SNACK, tile)) {
							g.setColor(Color.PINK);
							g.drawString(".", col * Tile.SIZE + 1, row * Tile.SIZE - 3 + offset_baseline);
						}
					} else {
						g.setColor(Rendering.alpha(Color.GREEN, 80));
						g.drawString("#", col * Tile.SIZE + 1, row * Tile.SIZE + offset_baseline - 1);
					}
				}
				world.bonusFood().ifPresent(bonus -> {
					Tile tile = bonus.location();
					g.setColor(Color.GREEN);
					String text = "";
					int col = tile.col;
					if (bonus.isPresent()) {
						text = "WIN " + bonus.value();
					} else if (bonus.isConsumed()) {
						text = "WON " + bonus.value() + " POINTS!";
						col -= 3;
					}
					g.drawString(text, (col - 3) * Tile.SIZE, tile.row * Tile.SIZE + offset_baseline - 1);
				});
			}
			world.houses().flatMap(House::doors).forEach(door -> {
				if (door.state == DoorState.CLOSED) {
					g.setColor(Color.PINK);
					door.tiles().forEach(tile -> {
						g.drawString("_", tile.x() + 1, tile.y());
					});
				}
			});
		};
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		if (messagesRenderer == null) {
			messagesRenderer = new MessagesRenderer();
			messagesRenderer.setFont($font("font"));
		}
		return messagesRenderer;
	}

	@Override
	public PacManSounds sounds() {
		return $value("sounds");
	}
}