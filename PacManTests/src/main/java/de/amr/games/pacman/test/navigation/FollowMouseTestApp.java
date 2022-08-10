package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.awt.AWTException;
import java.awt.Robot;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.components.Bed;

/**
 * Lets Blinky follow the mouse pointer.
 * 
 * @author Armin Reichert
 */
public class FollowMouseTestApp extends Application {

	public static void main(String[] args) {
		launch(FollowMouseTestApp.class, new PacManAppSettings(), args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Blinky catching mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestUI((PacManAppSettings) settings()));
	}
}

class FollowMouseTestUI extends TestController {

	private Tile mousePosition = null;

	public FollowMouseTestUI(PacManAppSettings settings) {
		super(settings);
	}

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
		var shell = Application.app().shell();
		if (shell.isPresent()) {
			if (mousePosition == null) {
				try {
					int x = shell.get().getX() + shell.get().getWidth() / 2;
					int y = shell.get().getY() + shell.get().getHeight() / 2;
					mousePosition = Tile.at(x / Tile.TS, y / Tile.TS);
					Robot robot = new Robot();
					robot.mouseMove(x, y);
				} catch (AWTException e) {
					var house = world.house(0).orElseThrow();
					Bed bed = house.bed(0);
					mousePosition = Tile.at(bed.minX(), bed.minY());
					e.printStackTrace();
				}
			}
			if (Mouse.moved()) {
				mousePosition = Tile.at(Mouse.getX() / Tile.TS, Mouse.getY() / Tile.TS);
			}
		}
		super.update();
	}
}