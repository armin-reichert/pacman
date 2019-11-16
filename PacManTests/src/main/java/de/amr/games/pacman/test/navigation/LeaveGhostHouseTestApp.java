package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.PacManGame;

public class LeaveGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(new LeaveGhostHouseTestApp(), args);
	}

	public LeaveGhostHouseTestApp() {
		settings.title = "Leave Ghost House";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		clock.setFrequency(20);
	}

	@Override
	public void init() {
		setController(new LeaveGhostHouseTestController());
	}
}