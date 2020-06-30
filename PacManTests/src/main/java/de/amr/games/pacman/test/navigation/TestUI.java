package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.controller.SpeedLimits;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class TestUI extends PlayView {

	public TestUI(PacManWorld world) {
		super(world, new Game(1, world.totalFoodCount()), new ArcadeTheme());
		world.pacMan().fnSpeedLimit = () -> SpeedLimits.pacManSpeedLimit(world.pacMan(), game);
		world.pacMan().game = game;
		world.ghosts().forEach(ghost -> {
			ghost.fnSpeedLimit = () -> SpeedLimits.ghostSpeedLimit(ghost, game);
			ghost.game = game;
		});
	}

	public TestUI() {
		this(Universe.arcadeWorld());
	}

}
