package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IGhostRenderer;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.api.PacManSounds;
import de.amr.games.pacman.view.theme.arcade.sounds.ArcadeSounds;
import de.amr.games.pacman.view.theme.common.AbstractTheme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.ScoreView;

public class ArcadeTheme extends AbstractTheme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private MessagesRenderer messagesRenderer;

	private ArcadeTheme() {
		super("ARCADE");
		put("font", Assets.storeTrueTypeFont("PressStart2P", "themes/arcade/PressStart2P-Regular.ttf", Font.PLAIN, 8));
		put("maze-flash-sec", 0.4f);
		put("sprites", new ArcadeThemeSprites());
		put("sounds", ArcadeSounds.SOUNDS);
	}

	@Override
	public IWorldRenderer worldRenderer(World world) {
		return new WorldRenderer();
	}

	@Override
	public View scoreView(World world, Game game) {
		ScoreView view = new ScoreView(game);
		Font font = $font("font");
		view.setFont(font);
		return view;
	}

	@Override
	public View livesCounterView(World world, Game game) {
		return new LivesCounterView(game);
	}

	@Override
	public View levelCounterView(World world, Game game) {
		return new LevelCounterView(game);
	}

	@Override
	public IPacManRenderer pacManRenderer(PacMan pacMan) {
		return new PacManRenderer();
	}

	@Override
	public IGhostRenderer ghostRenderer(Ghost ghost) {
		return new GhostRenderer(ghost);
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		if (messagesRenderer == null) {
			messagesRenderer = new MessagesRenderer();
			messagesRenderer.setFont($font("font"));
		}
		return messagesRenderer;
	}

	@Override
	public PacManSounds sounds() {
		return $value("sounds");
	}
}