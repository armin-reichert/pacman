package de.amr.games.pacman.view.theme;

public class PacManTheme {

	public static PacManAssets ASSETS;
	
	public static void init() {
		ASSETS = new ClassicPacManTheme();
	}
}