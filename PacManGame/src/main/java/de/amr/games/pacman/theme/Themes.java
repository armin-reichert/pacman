package de.amr.games.pacman.theme;

/**
 * Theme factory.
 * 
 * @author Armin Reichert
 */
public interface Themes {

	static Theme createTheme(String name) {
		switch (name) {
		case "Arcade":
			return new ArcadeTheme();
		case "MSX":
			return new MSXTheme();
		default:
			throw new IllegalArgumentException("Unknown theme: " + name);
		}
	}
}
