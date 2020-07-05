package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

public class ArcadeTheme implements Theme {

	@Override
	public IWorldRenderer createWorldRenderer(World world) {
		return new de.amr.games.pacman.view.theme.arcade.WorldRenderer(world);
	}

	@Override
	public IRenderer createScoreRenderer(Game game) {
		ScoreRenderer renderer = new de.amr.games.pacman.view.theme.common.ScoreRenderer(game);
		Font font = Assets.font("font.hud");
		renderer.setFont(font);
		return renderer;
	}

	@Override
	public IRenderer createLiveCounterRenderer(Game game) {
		return new de.amr.games.pacman.view.theme.arcade.LiveCounterRenderer(game);
	}

	@Override
	public IRenderer createLevelCounterRenderer(Game game) {
		return new de.amr.games.pacman.view.theme.arcade.LevelCounterRenderer(game);
	}

	@Override
	public IRenderer createPacManRenderer(World world, PacMan pacMan) {
		return new de.amr.games.pacman.view.theme.arcade.PacManRenderer(world, pacMan);
	}

	@Override
	public IRenderer createGhostRenderer(Ghost ghost) {
		return new de.amr.games.pacman.view.theme.arcade.GhostRenderer(ghost);
	}
}