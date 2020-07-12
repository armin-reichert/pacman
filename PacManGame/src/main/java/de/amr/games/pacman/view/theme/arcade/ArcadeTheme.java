package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.api.ThemeParameters;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.ParameterMap;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

public class ArcadeTheme implements Theme {

	public static final ParameterMap env = new ParameterMap();
	{
		env.put("maze-flash-sec", 0.4f);
	}

	public static final ArcadeThemeAssets ASSETS = new ArcadeThemeAssets();

	@Override
	public ThemeParameters env() {
		return env;
	}

	@Override
	public String name() {
		return "ARCADE";
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return new WorldRenderer(world);
	}

	@Override
	public IRenderer createScoreRenderer(World world, Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		Font font = ASSETS.messageFont;
		renderer.setFont(font);
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
		renderer.setFont(ASSETS.messageFont);
		return renderer;
	}
}