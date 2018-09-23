package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class ScatteringTestApp extends PacManApp {

	public static void main(String[] args) {
		theme = new ClassicPacManTheme();
		launch(new ScatteringTestApp());
	}

	public ScatteringTestApp() {
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestController());
	}
}