package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class InkyChaseTestApp extends PacManApp {

	public static void main(String[] args) {
		theme = new ClassicPacManTheme();
		launch(new InkyChaseTestApp());
	}

	public InkyChaseTestApp() {
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestController());
	}
}