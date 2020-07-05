package de.amr.games.pacman.view.theme;

import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * Interface for accessing Pac-Man game specific UI resources.
 * 
 * @author Armin Reichert
 */
public interface Theme {


	BufferedImage img_logo();

	Font fnt_text();

}