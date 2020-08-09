package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.IGameScoreRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.PacManSounds;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.PointsCounterRenderer;
import de.amr.games.pacman.view.core.ThemeParameters;
import de.amr.games.pacman.view.theme.arcade.ArcadeSounds;
import de.amr.games.pacman.view.theme.arcade.LivesCounterRenderer;

/**
 * A theme using simple geometric figures.
 * 
 * @author Armin Reichert
 *
 */
public class BlocksTheme extends ThemeParameters implements Theme {

	public static final BlocksTheme THEME = new BlocksTheme();

	private MessagesRenderer messagesRenderer;

	private BlocksTheme() {
		set("font", Assets.storeTrueTypeFont("ConcertOne", "themes/blocks/ConcertOne-Regular.ttf", Font.PLAIN, 10));
		set("maze-flash-sec", 0.5f);
		set("wall-color", new Color(139, 69, 19));
		set("ghost-colors", Map.of(
		//@formatter:off
			GhostPersonality.SHADOW, Color.RED,
			GhostPersonality.SPEEDY,  Color.PINK,
			GhostPersonality.BASHFUL,   Color.CYAN,
			GhostPersonality.POKEY,  Color.ORANGE
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
		return colorByPersonality.getOrDefault(ghost.getPersonality(), Color.WHITE);
	}

	Color symbolColor(String symbolName) {
		Map<String, Color> colorBySymbol = $value("symbol-colors");
		return colorBySymbol.getOrDefault(symbolName, Color.GREEN);
	}

	@Override
	public IWorldRenderer worldRenderer(World world) {
		return new WorldRenderer();
	}

	@Override
	public IGameScoreRenderer levelCounterRenderer() {
		return new LevelCounterRenderer();
	}

	@Override
	public IPacManRenderer pacManRenderer(PacMan pacMan) {
		return new PacManRenderer();
	}

	@Override
	public IGhostRenderer ghostRenderer(Ghost ghost) {
		return new GhostRenderer();
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
	public IGameScoreRenderer pointsCounterRenderer() {
		PointsCounterRenderer renderer = new PointsCounterRenderer();
		renderer.setFont($font("font"));
		return renderer;
	}

	@Override
	public IGameScoreRenderer livesCounterRenderer() {
		return new LivesCounterRenderer();
	}

	@Override
	public PacManSounds sounds() {
		return $value("sounds");
	}
}