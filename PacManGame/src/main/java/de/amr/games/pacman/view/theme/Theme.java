package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;

public interface Theme {

	IRenderer createGhostRenderer(Ghost ghost);

	IRenderer createPacManRenderer(World world, PacMan pacMan);

	IRenderer createLevelCounterRenderer( Game game);

	IRenderer createLiveCounterRenderer(Game game);

	IRenderer createScoreRenderer(Game game);

	IWorldRenderer createWorldRenderer(World world);

}
