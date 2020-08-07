package de.amr.games.pacman.view.theme.api;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.common.MessagesRenderer;

public interface Theme extends ThemeParameters {

	String name();

	IWorldRenderer worldRenderer(World world);

	IPacManRenderer pacManRenderer(PacMan pacMan);

	IGhostRenderer ghostRenderer(Ghost ghost);

	IGameRenderer levelCounterRenderer();

	IGameRenderer livesCounterRenderer();

	IGameRenderer scoreRenderer();

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}