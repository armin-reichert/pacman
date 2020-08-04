package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.api.PacManSounds;
import de.amr.games.pacman.view.theme.arcade.sounds.ArcadeSounds;
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

	public static final BlocksTheme THEME = new BlocksTheme();

	private MessagesRenderer messagesRenderer;

	private BlocksTheme() {
		super("BLOCKS");
		put("font", Assets.storeTrueTypeFont("ConcertOne", "themes/blocks/ConcertOne-Regular.ttf", Font.PLAIN, 10));
		put("maze-flash-sec", 0.5f);
		put("wall-color", new Color(139, 69, 19));
		put("ghost-colors", Map.of(
		//@formatter:off
			GhostPersonality.SHADOW, Color.RED,
			GhostPersonality.SPEEDY,  Color.PINK,
			GhostPersonality.BASHFUL,   Color.CYAN,
			GhostPersonality.POKEY,  Color.ORANGE
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
		put("sounds", ArcadeSounds.SOUNDS);
	}

	Color ghostColor(Ghost ghost) {
		Map<Integer, Color> colorByPersonality = $value("ghost-colors");
		return colorByPersonality.getOrDefault(ghost.getPersonality(), Color.WHITE);
	}

	Color symbolColor(String symbolName) {
		Map<String, Color> colorBySymbol = $value("symbol-colors");
		return colorBySymbol.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public IWorldRenderer worldRenderer(World world) {
		return new WorldRenderer((ArcadeWorld) world);
	}

	@Override
	public IRenderer scoreRenderer(World world, Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		renderer.setFont($font("font"));
		return renderer;
	}

	@Override
	public IRenderer livesCounterRenderer(World world, Game game) {
		return new LiveCounterRenderer(game);
	}

	@Override
	public IRenderer levelCounterRenderer(World world, Game game) {
		return new LevelCounterRenderer(game);
	}

	@Override
	public IPacManRenderer pacManRenderer(PacMan pacMan) {
		return new PacManRenderer(pacMan);
	}

	@Override
	public IRenderer ghostRenderer(Ghost ghost) {
		return new GhostRenderer(ghost);
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		if (messagesRenderer == null) {
			messagesRenderer = new MessagesRenderer();
			messagesRenderer.setFont($font("font").deriveFont(14f));
			messagesRenderer.setSmoothText(true);
		}
		return messagesRenderer;
	}

	@Override
	public PacManSounds sounds() {
		return $value("sounds");
	}
}