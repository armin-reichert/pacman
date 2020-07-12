package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IRenderer;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.api.ThemeParameters;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.ParameterMap;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

/**
 * A theme using simple geometric figures.
 * 
 * @author Armin Reichert
 *
 */
public class BlocksTheme implements Theme {

	public static final ParameterMap env = new ParameterMap();

	{
		env.put("font", Assets.storeTrueTypeFont("ConcertOne", "ConcertOne-Regular.ttf", Font.PLAIN, 10));
		env.put("maze-flash-sec", 0.5f);

		env.put("ghost-colors", Map.of(
		//@formatter:off
		Ghost.RED_GHOST,    Color.RED,
		Ghost.PINK_GHOST,   Color.PINK,
		Ghost.CYAN_GHOST,   Color.CYAN,
		Ghost.ORANGE_GHOST, Color.ORANGE
		//@formatter:on
		));

		env.put("symbol-colors", Map.of(
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

	public static Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colors = env.$value("ghost-colors");
		return colors.getOrDefault(ghost.getColor(), Color.WHITE);
	}

	public static Color symbolColor(String symbolName) {
		Map<String, Color> colors = env.$value("symbol-colors");
		return colors.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public ThemeParameters env() {
		return env;
	}

	@Override
	public String name() {
		return "BLOCKS";
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return new WorldRenderer(world);
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		renderer.setFont(env.$font("font"));
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
		renderer.setFont(env.$font("font").deriveFont(14f));
		renderer.setSmoothText(true);
		return renderer;
	}
}