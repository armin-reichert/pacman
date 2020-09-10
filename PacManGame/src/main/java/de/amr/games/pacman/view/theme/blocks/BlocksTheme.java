package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IMessagesRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IPacManSounds;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.PointsCounterRenderer;
import de.amr.games.pacman.view.common.Rendering;
import de.amr.games.pacman.view.core.ThemeParameters;
import de.amr.games.pacman.view.theme.arcade.ArcadeSounds;

/**
 * A theme using simple geometric figures.
 * 
 * @author Armin Reichert
 */
public class BlocksTheme extends ThemeParameters implements Theme {

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
		Map<Integer, Color> colorByPersonality = $value("ghost-colors");
		return colorByPersonality.getOrDefault(ghost.personality, Color.WHITE);
	}

	Color symbolColor(String symbolName) {
		Map<String, Color> colorBySymbol = $value("symbol-colors");
		return colorBySymbol.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public IWorldRenderer worldRenderer() {
		return new WorldRenderer();
	}

	@Override
	public IPacManRenderer pacManRenderer() {
		return new PacManRenderer();
	}

	@Override
	public IGhostRenderer ghostRenderer() {
		return new GhostRenderer();
	}

	@Override
	public IMessagesRenderer messagesRenderer() {
		MessagesRenderer messagesRenderer = new MessagesRenderer();
		messagesRenderer.setFont($font("font").deriveFont(14f));
		messagesRenderer.setTextAntialiasing(true);
		return messagesRenderer;
	}

	@Override
	public IGameRenderer pointsCounterRenderer() {
		PointsCounterRenderer r = new PointsCounterRenderer();
		r.setFont($font("font"));
		return r;
	}

	@Override
	public IGameRenderer livesCounterRenderer() {
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
	public IGameRenderer levelCounterRenderer() {
		return (Graphics2D g, PacManGame level) -> {
			Rendering.smoothOn(g);
			int levels = level.levelCounter.size();
			for (int i = 0, x = -2 * Tile.SIZE; i < Math.min(7, levels); ++i, x -= 2 * Tile.SIZE) {
				ArcadeBonus symbol = ArcadeBonus.valueOf(level.levelCounter.get(levels > 7 ? levels - 7 + i : i));
				g.setColor(BlocksTheme.THEME.symbolColor(symbol.name()));
				g.drawOval(x, 0, Tile.SIZE, Tile.SIZE);
			}
			Rendering.smoothOff(g);
		};
	}

	@Override
	public IPacManSounds sounds() {
		return $value("sounds");
	}
}