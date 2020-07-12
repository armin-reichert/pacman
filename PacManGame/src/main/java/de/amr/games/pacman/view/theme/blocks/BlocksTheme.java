package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.common.AbstractTheme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

/**
 * A theme using simple geometric figures.
 * 
 * @author Armin Reichert
 *
 */
public class BlocksTheme extends AbstractTheme {

	public static final BlocksTheme IT = new BlocksTheme();

	private BlocksTheme() {
		super("BLOCKS");
		put("font", Assets.storeTrueTypeFont("ConcertOne", "themes/blocks/ConcertOne-Regular.ttf", Font.PLAIN, 10));
		put("maze-flash-sec", 0.5f);

		put("ghost-colors", Map.of(
		//@formatter:off
		Ghost.RED_GHOST,    Color.RED,
		Ghost.PINK_GHOST,   Color.PINK,
		Ghost.CYAN_GHOST,   Color.CYAN,
		Ghost.ORANGE_GHOST, Color.ORANGE
		//@formatter:on
		));

		put("symbol-colors", Map.of(
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
	}

	public Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colors = $value("ghost-colors");
		return colors.getOrDefault(ghost.getColor(), Color.WHITE);
	}

	public Color symbolColor(String symbolName) {
		Map<String, Color> colors = $value("symbol-colors");
		return colors.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return new WorldRenderer(world);
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		renderer.setFont($font("font"));
		renderer.setSmoothText(true);
		return renderer;
	}

	@Override
	public IRenderer createLiveCounterRenderer(World world, Game game) {
		return new LiveCounterRenderer(game);
	}

	@Override
	public IRenderer createLevelCounterRenderer(World world, Game game) {
		return new LevelCounterRenderer(game);
	}

	@Override
	public IPacManRenderer createPacManRenderer(PacMan pacMan) {
		return new PacManRenderer(pacMan);
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return new GhostRenderer(ghost);
	}

	@Override
	public MessagesRenderer createMessagesRenderer() {
		MessagesRenderer renderer = new MessagesRenderer();
		renderer.setFont($font("font").deriveFont(14f));
		renderer.setSmoothText(true);
		return renderer;
	}
}