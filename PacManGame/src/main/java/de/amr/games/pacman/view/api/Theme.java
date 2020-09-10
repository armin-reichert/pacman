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

	WorldRenderer worldRenderer();

	PacManRenderer pacManRenderer();

	GhostRenderer ghostRenderer();

	GameRenderer levelCounterRenderer();

	GameRenderer livesCounterRenderer();

	GameRenderer gameScoreRenderer();

	MessagesRenderer messagesRenderer();

	PacManSounds sounds();
}