package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import java.awt.AWTException;
import java.awt.Robot;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.ui.AppShell;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.test.TestUI;

public class FollowMouseTestApp extends Application {

	public static void main(String[] args) {
		launch(FollowMouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Blinky Follows Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestUI());
	}
}

class FollowMouseTestUI extends TestUI {

	private Tile mousePosition = null;

	@Override
	public void init() {
		super.init();
		include(blinky);
		blinky.init();
		you(blinky).when(CHASING).headFor().tile(() -> mousePosition).ok();
		blinky.ai.setState(CHASING);
		view.turnRoutesOn();
		view.turnGridOn();
	}

	@Override
	public void update() {
		if (mousePosition == null) {
			try {
				AppShell shell = Application.app().shell().get();
				int x = shell.getX() + shell.getWidth() / 2;
				int y = shell.getY() + shell.getHeight() / 2;
				mousePosition = Tile.at(x / Tile.SIZE, y / Tile.SIZE);
				Robot robot = new Robot();
				robot.mouseMove(x, y);
			} catch (AWTException e) {
				Bed bed = world.house(0).bed(0);
				mousePosition = Tile.at(bed.col(), bed.row());
				e.printStackTrace();
			}
		}
		if (Mouse.moved()) {
			mousePosition = Tile.at(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
		}
		super.update();
	}
}