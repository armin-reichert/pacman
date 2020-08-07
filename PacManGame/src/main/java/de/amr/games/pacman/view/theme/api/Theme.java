package de.amr.games.pacman.view.theme.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.common.MessagesRenderer;

public interface Theme extends ThemeParameters {

	String name();

	default void renderWorld(Graphics2D g, World world) {
		worldRenderer(world).render(g, world);
	}
	
	default void renderPacMan(Graphics2D g, PacMan pacMan) {
		pacManRenderer(pacMan).render(g, pacMan);
	}
	
	default void renderGhost(Graphics2D g, Ghost ghost) {
		ghostRenderer(ghost).render(g, ghost);
	}

	IWorldRenderer worldRenderer(World world);
	
	IPacManRenderer pacManRenderer(PacMan pacMan);

	IGhostRenderer ghostRenderer(Ghost ghost);

	IGameRenderer levelCounterRenderer();

	IGameRenderer livesCounterRenderer();

	IGameRenderer scoreRenderer();

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}