package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

public interface Theme {

	String name();

	IRenderer createGhostRenderer(Ghost ghost);

	IPacManRenderer createPacManRenderer(World world);

	IRenderer createLevelCounterRenderer(World world, Game game);

	IRenderer createLiveCounterRenderer(World world, Game game);

	IRenderer createScoreRenderer(World world, Game game);

	IWorldRenderer createWorldRenderer(World world);

	MessagesRenderer createMessagesRenderer();
}