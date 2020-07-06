package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

public interface Theme {

	String name();

	IRenderer createGhostRenderer(Ghost ghost);

	IRenderer createPacManRenderer(World world);

	IRenderer createLevelCounterRenderer(Game game);

	IRenderer createLiveCounterRenderer(Game game);

	IRenderer createScoreRenderer(Game game);

	IWorldRenderer createWorldRenderer(World world);

	MessagesRenderer createMessagesRenderer();
}