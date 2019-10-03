package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class EscapeIntoCornerTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new EscapeIntoCornerTestApp(), args);
	}

	public EscapeIntoCornerTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestController(theme));
	}
}