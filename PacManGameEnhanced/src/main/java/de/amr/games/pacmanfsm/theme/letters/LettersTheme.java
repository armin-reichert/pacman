/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.theme.letters;

import static de.amr.games.pacmanfsm.controller.creatures.pacman.PacManState.COLLAPSING;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.Transform;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeFood;
import de.amr.games.pacmanfsm.model.world.components.Door.DoorState;
import de.amr.games.pacmanfsm.model.world.components.House;
import de.amr.games.pacmanfsm.theme.api.GameRenderer;
import de.amr.games.pacmanfsm.theme.api.GhostRenderer;
import de.amr.games.pacmanfsm.theme.api.MessagesRenderer;
import de.amr.games.pacmanfsm.theme.api.PacManRenderer;
import de.amr.games.pacmanfsm.theme.api.Theme;
import de.amr.games.pacmanfsm.theme.api.WorldRenderer;
import de.amr.games.pacmanfsm.theme.arcade.ArcadeSounds;
import de.amr.games.pacmanfsm.theme.core.ThemeParameterMap;
import de.amr.games.pacmanfsm.view.api.PacManGameSounds;
import de.amr.games.pacmanfsm.view.common.DefaultMessagesRenderer;
import de.amr.games.pacmanfsm.view.common.Rendering;

/**
 * Theme using letters and other non-graphic characters.
 * 
 * @author Armin Reichert
 */
public class LettersTheme extends ThemeParameterMap implements Theme {

	private static final String OFFSET_BASELINE = "offset-baseline";
	public static final LettersTheme THEME = new LettersTheme();

	private LettersTheme() {
		set("font", new Font(Font.MONOSPACED, Font.BOLD, Tile.TS));
		set(OFFSET_BASELINE, Tile.TS - 1);
		set("ghost-colors", Map.of(
		//@formatter:off
			GhostPersonality.SHADOW,  Color.RED,
			GhostPersonality.SPEEDY,  Color.PINK,
			GhostPersonality.BASHFUL, Color.CYAN,
			GhostPersonality.POKEY,   Color.ORANGE
		//@formatter:on
		));
		set("sounds", ArcadeSounds.SOUNDS);
	}

	@Override
	public String name() {
		return "LETTERS";
	}

	Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colorByPersonality = asValue("ghost-colors");
		return colorByPersonality.getOrDefault(ghost.personality.ordinal(), Color.WHITE);
	}

	String ghostLetter(Ghost ghost) {
		if (ghost.ai.getState() == null) {
			return ghost.name.substring(0, 1);
		}
		return switch (ghost.ai.getState()) {
		case FRIGHTENED -> ghost.name.substring(0, 1).toLowerCase();
		case DEAD, ENTERING_HOUSE -> Rendering.INFTY;
		default -> ghost.name.substring(0, 1);
		};
	}

	@Override
	public GhostRenderer ghostRenderer() {
		return (g, ghost) -> {
			if (ghost.visible) {
				Font font = asFont("font");
				int offsetBaseline = asInt(OFFSET_BASELINE);
				g.setFont(font.deriveFont((float) ghost.tf.width));
				g.setColor(ghostColor(ghost));
				if (ghost.bounty > 0) {
					g.drawString("" + ghost.bounty, ghost.tf.x, ghost.tf.y + offsetBaseline);
				} else {
					g.drawString(ghostLetter(ghost), ghost.tf.x, ghost.tf.y + offsetBaseline);
				}
			}
		};
	}

	@Override
	public PacManRenderer pacManRenderer() {
		return (g, pacMan) -> {
			if (pacMan.visible) {
				Transform tf = pacMan.tf;
				int offsetBaseline = asInt(OFFSET_BASELINE);
				g.setFont(asFont("font").deriveFont((float) tf.width));
				g.setColor(Color.YELLOW);
				String letter = pacMan.ai.is(COLLAPSING) ? "\u2668" : "O";
				g.drawString(letter, tf.x, tf.y + offsetBaseline);
			}
		};
	}

	@Override
	public GameRenderer levelCounterRenderer() {
		return (g, level) -> {
			Font font = asFont("font");
			int offsetBaseline = asInt(OFFSET_BASELINE);
			String text = String.format("Level: %d (%s)", level.level, level.bonusSymbol);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(text, -15 * Tile.TS, Tile.TS + offsetBaseline);
		};
	}

	@Override
	public GameRenderer livesCounterRenderer() {
		return (g, level) -> {
			Font font = asFont("font");
			int offsetBaseline = asInt(OFFSET_BASELINE);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(String.format("Lives: %d", level.lives), 0, Tile.TS + offsetBaseline);
		};
	}

	@Override
	public GameRenderer gameScoreRenderer() {
		return (g, level) -> {
			Font font = asFont("font");
			int offsetBaseline = asInt(OFFSET_BASELINE);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			g.drawString(" Score          Highscore        Pellets", 0, offsetBaseline);
			g.drawString(
					String.format(" %08d       %08d         %03d", level.score, level.hiscore.points, level.remainingFoodCount()),
					0, Tile.TS + offsetBaseline);
		};
	}

	@Override
	public WorldRenderer worldRenderer() {
		return (g, world) -> {
			Font font = asFont("font");
			int offsetBaseline = asInt(OFFSET_BASELINE);
			g.setFont(font);
			for (int row = 3; row < world.height() - 2; ++row) {
				for (int col = 0; col < world.width(); ++col) {
					Tile tile = Tile.at(col, row);
					if (world.isAccessible(tile)) {
						if (world.hasFood(ArcadeFood.ENERGIZER, tile) && Application.app().clock().getTotalTicks() % 60 < 30) {
							g.setColor(Color.PINK);
							g.drawString("Ö", col * Tile.TS + 2, row * Tile.TS + offsetBaseline);
						}
						if (world.hasFood(ArcadeFood.PELLET, tile)) {
							g.setColor(Color.PINK);
							g.drawString(".", col * Tile.TS + 1, row * Tile.TS - 3 + offsetBaseline);
						}
					} else {
						g.setColor(Rendering.alpha(Color.GREEN, 80));
						g.drawString("#", col * Tile.TS + 1, row * Tile.TS + offsetBaseline - 1);
					}
				}
				world.temporaryFood().ifPresent(bonus -> {
					Tile tile = bonus.location();
					g.setColor(Color.GREEN);
					String text = "";
					int col = tile.col;
					if (bonus.isActive() && !bonus.isConsumed()) {
						text = "BONUS " + bonus.value();
						col = tile.col - 1;
					} else if (bonus.isConsumed()) {
						text = "WON " + bonus.value() + " POINTS!";
						col = tile.col - 3;
					}
					g.drawString(text, col * Tile.TS, tile.row * Tile.TS + offsetBaseline - 1);
				});
			}
			world.houses().flatMap(House::doors).forEach(door -> {
				if (door.state == DoorState.CLOSED) {
					g.setColor(Color.PINK);
					door.tiles().forEach(tile -> g.drawString("_", tile.x() + 1, tile.y()));
				}
			});
		};
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		DefaultMessagesRenderer messagesRenderer = new DefaultMessagesRenderer();
		messagesRenderer.setFont(asFont("font"));
		return messagesRenderer;
	}

	@Override
	public PacManGameSounds sounds() {
		return asValue("sounds");
	}
}