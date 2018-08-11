package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;

/**
 * Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		float scale = 1f;
		if (args.length > 0) {
			try {
				scale = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				Application.LOGGER.info("Illegal scaling value: " + args[0]);
			}
		}
		launch(new PacManApp(scale));
	}

	public PacManApp(float scale) {
		settings.width = 28 * Game.TS;
		settings.height = 36 * Game.TS;
		settings.scale = scale;
		settings.title = "Armin's Pac-Man";
	}

	@Override
	public void init() {
		setController(new GameController());
	}
}