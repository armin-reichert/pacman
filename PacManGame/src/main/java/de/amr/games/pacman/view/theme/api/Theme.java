package de.amr.games.pacman.view.theme.api;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

public interface Theme extends ThemeParameters {

	String name();

	IRenderer ghostRenderer(Ghost ghost);

	IPacManRenderer pacManRenderer(PacMan pacMan);

	IRenderer levelCounterRenderer(World world, Game game);

	IRenderer livesCounterRenderer(World world, Game game);

	IRenderer scoreRenderer(World world, Game game);

	IWorldRenderer worldRenderer(World world);

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}