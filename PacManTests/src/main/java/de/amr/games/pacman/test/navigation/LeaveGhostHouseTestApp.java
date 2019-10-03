package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class LeaveGhostHouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new LeaveGhostHouseTestApp(), args);
	}

	public LeaveGhostHouseTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Leave Ghost House";
		clock.setFrequency(20);
	}

	@Override
	public void init() {
		setController(new LeaveGhostHouseTestController(theme));
	}
}