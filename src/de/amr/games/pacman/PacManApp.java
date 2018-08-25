package de.amr.games.pacman;

import java.util.Arrays;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		float scale = 2f;
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
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		PacManTheme.init();
		loadSounds();
		setController(new GameController());
	}

	private void loadSounds() {
		//@formatter:off
		Arrays.asList("die", "eat-fruit", "eat-ghost", "eat-pill", "eating", "extra-life", 
				"insert-coin", "ready", "siren", "waza").stream()
			.map(name -> "sfx/" + name + ".mp3")
			.map(path -> Assets.sound(path));
		//@formatter:on
	}
}