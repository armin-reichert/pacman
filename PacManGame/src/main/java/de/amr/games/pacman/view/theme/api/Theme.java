package de.amr.games.pacman.view.theme.api;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

public interface Theme extends ThemeParameters {

	String name();

	IRenderer createGhostRenderer(Ghost ghost);

	IPacManRenderer createPacManRenderer(PacMan pacMan);

	IRenderer createLevelCounterRenderer(World world, Game game);

	IRenderer createLiveCounterRenderer(World world, Game game);

	IRenderer createScoreRenderer(World world, Game game);

	IWorldRenderer createWorldRenderer(World world);

	MessagesRenderer createMessagesRenderer();

	PacManSounds createSounds();
}