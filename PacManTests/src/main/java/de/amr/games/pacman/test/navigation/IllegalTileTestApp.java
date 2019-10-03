package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class IllegalTileTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new IllegalTileTestApp(), args);
	}

	public IllegalTileTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Follow Illegal Tile";
	}

	@Override
	public void init() {
		setController(new IllegalTileTestController(theme));
	}
}