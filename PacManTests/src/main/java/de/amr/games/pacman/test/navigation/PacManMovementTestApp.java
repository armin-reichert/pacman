package de.amr.games.pacman.test.navigation;

import java.awt.Color;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class PacManMovementTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new PacManMovementTestApp(), args);
	}

	public PacManMovementTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Pac-Man Movement";
		settings.bgColor = Color.GREEN;
	}

	@Override
	public void init() {
		setController(new PacManMovementTestController(theme));
	}
}