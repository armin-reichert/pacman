package de.amr.games.pacman.view.theme.blocks;

import java.awt.Font;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.Theming.ThemeName;
import de.amr.games.pacman.view.theme.common.ActorRoutesRenderer;
import de.amr.games.pacman.view.theme.common.NullRenderer;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

public class BlocksTheme implements Theme {

	@Override
	public ThemeName name() {
		return ThemeName.BLOCKS;
	}

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return new WorldRenderer(world);
	}

	@Override
	public IRenderer createActorRoutesRenderer(World world) {
		return new ActorRoutesRenderer(world);
	}

	@Override
	public IRenderer createActorStatesRenderer(World world) {
		return new NullRenderer();
	}

	@Override
	public IRenderer createScoreRenderer(Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 8);
		renderer.setFont(font);
		return renderer;
	}

	@Override
	public IRenderer createLiveCounterRenderer(Game game) {
		return new LiveCounterRenderer(game);
	}

	@Override
	public IRenderer createLevelCounterRenderer(Game game) {
		return new LevelCounterRenderer(game);
	}

	@Override
	public IRenderer createPacManRenderer(World world, PacMan pacMan) {
		return new PacManRenderer(world, pacMan);
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return new GhostRenderer(ghost);
	}
}