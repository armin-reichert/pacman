package de.amr.games.pacman.theme;

/**
 * Theme factory.
 * 
 * @author Armin Reichert
 */
public interface Themes {

	static Theme createTheme(String name) {
		switch (name.toLowerCase()) {
		case "arcade":
			return new ArcadeTheme();
		case "msx":
			return new MSXTheme();
		case "sharpx68000":
			return new SharpX68000Theme();
		default:
			throw new IllegalArgumentException("Unknown theme: " + name);
		}
	}
}
