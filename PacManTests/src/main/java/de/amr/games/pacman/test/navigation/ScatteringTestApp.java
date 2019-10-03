package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class ScatteringTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new ScatteringTestApp(), args);
	}

	public ScatteringTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestController(theme));
	}
}