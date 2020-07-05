package de.amr.games.pacman.view.theme;

import java.awt.Font;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;

public class Theming {

	public enum Theme {
		ARCADE, BLOCKS
	}

	public static IWorldRenderer createWorldRenderer(Theme theme, World world) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.WorldRenderer(world);
		} else {
			return new de.amr.games.pacman.view.theme.blocks.WorldRenderer(world);
		}
	}

	public static IRenderer createScoreRenderer(Theme theme, Game game) {
		ScoreRenderer renderer = new de.amr.games.pacman.view.theme.common.ScoreRenderer(game);
		Font font = theme == Theme.ARCADE ? Assets.font("font.hud") : new Font(Font.MONOSPACED, Font.BOLD, 8);
		renderer.setFont(font);
		return renderer;
	}

	public static IRenderer createLiveCounterRenderer(Theme theme, Game game) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.LiveCounterRenderer(game);
		} else {
			return new de.amr.games.pacman.view.theme.blocks.LiveCounterRenderer(game);
		}
	}

	public static IRenderer createLevelCounterRenderer(Theme theme, Game game) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.LevelCounterRenderer(game);
		} else {
			return new de.amr.games.pacman.view.theme.blocks.LevelCounterRenderer(game);
		}
	}

	public static IRenderer createPacManRenderer(Theme theme, World world, PacMan pacMan) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.PacManRenderer(world, pacMan);
		} else if (theme == Theme.BLOCKS) {
			return new de.amr.games.pacman.view.theme.blocks.PacManRenderer(world, pacMan);
		}
		throw new IllegalArgumentException("Unknown style " + theme);
	}

	public static IRenderer createGhostRenderer(Theme theme, Ghost ghost) {
		if (theme == Theme.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.GhostRenderer(ghost);
		} else if (theme == Theme.BLOCKS) {
			return new de.amr.games.pacman.view.theme.blocks.GhostRenderer(ghost);
		}
		throw new IllegalArgumentException("Unknown style " + theme);
	}
}