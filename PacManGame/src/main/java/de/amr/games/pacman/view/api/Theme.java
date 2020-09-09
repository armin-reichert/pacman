package de.amr.games.pacman.view.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.common.MessagesRenderer;

/**
 * Interface implemented by every theme.
 * 
 * @author Armin Reichert
 */
public interface Theme {

	String name();

	int $int(String key);

	float $float(String key);

	Font $font(String key);

	Color $color(String key);

	Image $image(String key);

	<T> T $value(String key);

	IWorldRenderer worldRenderer(World world);

	IPacManRenderer pacManRenderer(PacMan pacMan);

	IGhostRenderer ghostRenderer(Ghost ghost);

	IGameRenderer levelCounterRenderer();

	IGameRenderer livesCounterRenderer();

	IGameRenderer pointsCounterRenderer();

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}