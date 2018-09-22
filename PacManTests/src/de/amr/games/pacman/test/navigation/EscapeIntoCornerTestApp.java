package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class EscapeIntoCornerTestApp extends PacManApp {

	public static void main(String[] args) {
		theme = new ClassicPacManTheme();
		launch(new EscapeIntoCornerTestApp(), args);
	}

	public EscapeIntoCornerTestApp() {
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestController());
	}
}