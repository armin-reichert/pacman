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
package de.amr.games.pacman.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.theme.api.GameRenderer;
import de.amr.games.pacman.theme.api.GhostRenderer;
import de.amr.games.pacman.theme.api.MessagesRenderer;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.theme.api.WorldRenderer;
import de.amr.games.pacman.theme.arcade.ArcadeSounds;
import de.amr.games.pacman.theme.core.ThemeParameterMap;
import de.amr.games.pacman.view.api.PacManGameSounds;
import de.amr.games.pacman.view.common.DefaultGameScoreRenderer;
import de.amr.games.pacman.view.common.DefaultMessagesRenderer;
import de.amr.games.pacman.view.common.Rendering;

/**
 * A theme using simple geometric figures.
 * 
 * @author Armin Reichert
 */
public class BlocksTheme extends ThemeParameterMap implements Theme {

	public static final BlocksTheme THEME = new BlocksTheme();

	private BlocksTheme() {
		set("font", Assets.storeTrueTypeFont("ConcertOne", "themes/blocks/ConcertOne-Regular.ttf", Font.PLAIN, 10));
		set("maze-flash-sec", 0.5f);
		set("wall-color", new Color(139, 69, 19));
		set("ghost-colors", Map.of(
		//@formatter:off
			GhostPersonality.SHADOW,  Color.RED,
			GhostPersonality.SPEEDY,  Color.PINK,
			GhostPersonality.BASHFUL, Color.CYAN,
			GhostPersonality.POKEY,   Color.ORANGE
		//@formatter:on
		));
		set("symbol-colors", Map.of(
		//@formatter:off
			"APPLE",      Color.RED,		
			"BELL",       Color.YELLOW,
			"CHERRIES",   Color.RED,
			"GALAXIAN",   Color.BLUE,
			"GRAPES",     Color.GREEN,
			"KEY",        Color.BLUE,
			"PEACH",      Color.ORANGE,
			"STRAWBERRY", Color.RED
		//@formatter:on
		));
		set("sounds", ArcadeSounds.SOUNDS);
	}

	@Override
	public String name() {
		return "BLOCKS";
	}

	Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colorByPersonality = asValue("ghost-colors");
		return colorByPersonality.getOrDefault(ghost.personality, Color.WHITE);
	}

	Color symbolColor(String symbolName) {
		Map<String, Color> colorBySymbol = asValue("symbol-colors");
		return colorBySymbol.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public WorldRenderer worldRenderer() {
		return new BlocksWorldRenderer();
	}

	@Override
	public PacManRenderer pacManRenderer() {
		return new BlocksPacManRenderer();
	}

	@Override
	public GhostRenderer ghostRenderer() {
		return new BlocksGhostRenderer();
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		DefaultMessagesRenderer messagesRenderer = new DefaultMessagesRenderer();
		messagesRenderer.setFont(asFont("font").deriveFont(14f));
		messagesRenderer.setTextAntialiasing(true);
		return messagesRenderer;
	}

	@Override
	public GameRenderer gameScoreRenderer() {
		DefaultGameScoreRenderer r = new DefaultGameScoreRenderer();
		r.setFont(asFont("font"));
		return r;
	}

	@Override
	public GameRenderer livesCounterRenderer() {
		return (Graphics2D g, PacManGame level) -> {
			Rendering.smoothOn(g);
			g.setColor(Color.YELLOW);
			for (int i = 0, x = 0; i < level.lives; ++i, x += 2 * Tile.SIZE) {
				g.fillOval(x, 0, Tile.SIZE, Tile.SIZE);
			}
			Rendering.smoothOff(g);
		};
	}

	@Override
	public GameRenderer levelCounterRenderer() {
		return (Graphics2D g, PacManGame level) -> {
			Rendering.smoothOn(g);
			int levels = level.levelCounter.size();
			for (int i = 0, x = -2 * Tile.SIZE; i < Math.min(7, levels); ++i, x -= 2 * Tile.SIZE) {
				String symbolName = level.levelCounter.get(levels > 7 ? i + levels - 7 : i);
				ArcadeBonus.Symbol symbol = ArcadeBonus.Symbol.valueOf(symbolName);
				g.setColor(BlocksTheme.THEME.symbolColor(symbol.name()));
				g.drawOval(x, 0, Tile.SIZE, Tile.SIZE);
			}
			Rendering.smoothOff(g);
		};
	}

	@Override
	public PacManGameSounds sounds() {
		return asValue("sounds");
	}
}