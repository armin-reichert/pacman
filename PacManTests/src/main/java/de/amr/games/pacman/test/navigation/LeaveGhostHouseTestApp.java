package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.Worlds;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class LeaveGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(LeaveGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Leave Ghost House";
	}

	@Override
	public void init() {
		clock().setTargetFrameRate(10);
		setController(new LeaveGhostHouseTestUI(Worlds.arcade()));
	}
}

class LeaveGhostHouseTestUI extends PlayView {

	public LeaveGhostHouseTestUI(PacManWorld world) {
		super(world, new Game(world, 1), new ArcadeTheme());
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		world.takePart(world.inky);
		world.inky.subsequentState = SCATTERING;
		showMessage("Press SPACE to unlock", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE) && world.inky.is(GhostState.LOCKED)) {
			world.inky.process(new GhostUnlockedEvent());
			clearMessage();
		}
		world.inky.update();
		super.update();
	}
}