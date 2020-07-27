package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.PacManSounds;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.common.AbstractTheme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.ScoreRenderer;
import de.amr.games.pacman.view.theme.sound.ArcadeSounds;

public class ArcadeTheme extends AbstractTheme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private ArcadeTheme() {
		super("ARCADE");
		put("font", Assets.storeTrueTypeFont("PressStart2P", "themes/arcade/PressStart2P-Regular.ttf", Font.PLAIN, 8));
		put("maze-flash-sec", 0.4f);
		put("sprites", new ArcadeThemeSprites());
	}

	@Override
	public IWorldRenderer worldRenderer(World world) {
		return new WorldRenderer(world);
	}

	@Override
	public IRenderer scoreRenderer(World world, Game game) {
		ScoreRenderer renderer = new ScoreRenderer(game);
		Font font = $font("font");
		renderer.setFont(font);
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
		MessagesRenderer renderer = new MessagesRenderer();
		renderer.setFont($font("font"));
		return renderer;
	}

	@Override
	public PacManSounds sounds() {
		return new ArcadeSounds();
	}
}