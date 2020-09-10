package de.amr.games.pacman.view.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

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

	IWorldRenderer worldRenderer();

	IPacManRenderer pacManRenderer();

	IGhostRenderer ghostRenderer();

	IGameRenderer levelCounterRenderer();

	IGameRenderer livesCounterRenderer();

	IGameRenderer pointsCounterRenderer();

	IMessagesRenderer messagesRenderer();

	PacManSounds sounds();
}