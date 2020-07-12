package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.world.arcade.Symbol;
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

public class BlocksTheme implements Theme {

	public static final ParameterMap env = new ParameterMap();

	{
		env.put("font", Assets.storeTrueTypeFont("ConcertOne", "ConcertOne-Regular.ttf", Font.PLAIN, 10));
		env.put("maze-flash-sec", 0.5f);
	}

	public static final Color GHOST_COLORS[] = new Color[4];

	static {
		GHOST_COLORS[Ghost.RED_GHOST] = Color.RED;
		GHOST_COLORS[Ghost.PINK_GHOST] = Color.PINK;
		GHOST_COLORS[Ghost.CYAN_GHOST] = Color.CYAN;
		GHOST_COLORS[Ghost.ORANGE_GHOST] = Color.ORANGE;
	}

	public static Color ghostColor(Ghost ghost) {
		return GHOST_COLORS[ghost.getColor()];
	}

	public static Color symbolColor(String symbolName) {
		Symbol symbol = Stream.of(Symbol.values()).filter(s -> s.name().equals(symbolName)).findFirst().get();
		switch (symbol) {
		case APPLE:
			return Color.RED;
		case BELL:
			return Color.YELLOW;
		case CHERRIES:
			return Color.RED;
		case GALAXIAN:
			return Color.BLUE;
		case GRAPES:
			return Color.GREEN;
		case KEY:
			return Color.BLUE;
		case PEACH:
			return Color.ORANGE;
		case STRAWBERRY:
			return Color.RED;
		default:
			return Color.GREEN;
		}
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