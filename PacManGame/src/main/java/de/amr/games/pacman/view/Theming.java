package de.amr.games.pacman.view;

import java.awt.Font;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.render.IRenderer;
import de.amr.games.pacman.view.render.IWorldRenderer;
import de.amr.games.pacman.view.render.common.ScoreRenderer;

public class Theming {

	public enum Theme {
		ARCADE, BLOCKS
	}

	public static IWorldRenderer createWorldRenderer(Theme theme, World world) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.WorldRenderer(world);
		} else {
			return new de.amr.games.pacman.view.render.block.WorldRenderer(world);
		}
	}

	public static IRenderer createScoreRenderer(Theme theme, Game game) {
		ScoreRenderer renderer = new de.amr.games.pacman.view.render.common.ScoreRenderer(game);
		Font font = theme == Theme.ARCADE ? Assets.font("font.hud") : new Font(Font.MONOSPACED, Font.BOLD, 8);
		renderer.setFont(font);
		return renderer;
	}

	public static IRenderer createLiveCounterRenderer(Theme theme, Game game) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.LiveCounterRenderer(game);
		} else {
			return new de.amr.games.pacman.view.render.block.LiveCounterRenderer(game);
		}
	}

	public static IRenderer createLevelCounterRenderer(Theme theme, Game game) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.LevelCounterRenderer(game);
		} else {
			return new de.amr.games.pacman.view.render.block.LevelCounterRenderer(game);
		}
	}

	public static IRenderer createPacManRenderer(Theme theme, World world, PacMan pacMan) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.PacManRenderer(world, pacMan);
		} else if (theme == Theme.BLOCKS) {
			return new de.amr.games.pacman.view.render.block.PacManRenderer(world, pacMan);
		}
		throw new IllegalArgumentException("Unknown style " + theme);
	}

	public static IRenderer createGhostRenderer(Theme theme, Ghost ghost) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.GhostRenderer(ghost);
		} else if (theme == Theme.BLOCKS) {
			return new de.amr.games.pacman.view.render.block.GhostRenderer(ghost);
		}
		throw new IllegalArgumentException("Unknown style " + theme);
	}
}