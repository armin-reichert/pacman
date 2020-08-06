package de.amr.games.pacman.view.theme.api;

import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

public interface Theme extends ThemeParameters {

	String name();

	IGhostRenderer ghostRenderer(Ghost ghost);

	IPacManRenderer pacManRenderer(PacMan pacMan);

	View levelCounterView(World world, Game game);

	View livesCounterView(World world, Game game);

	View scoreView(World world, Game game);

	IWorldRenderer worldRenderer(World world);

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}