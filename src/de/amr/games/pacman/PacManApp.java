package de.amr.games.pacman;

import java.awt.EventQueue;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		LOGGER.setLevel(Level.INFO);
		float scale = 2f;
		if (args.length > 0) {
			try {
				scale = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				LOGGER.info("Illegal scaling value: " + args[0]);
			}
		}
		launch(new PacManApp(scale));
	}

	public static PacManTheme THEME;

	public PacManApp(float scale) {
		settings.width = 28 * Game.TS;
		settings.height = 36 * Game.TS;
		settings.scale = scale;
		settings.title = "Armin's Pac-Man";
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.fullScreenOnStart = false;
		try {
			THEME = ClassicPacManTheme.class.newInstance();
		} catch (Exception e) {
			LOGGER.info("Could not create theme");
			throw new RuntimeException(e);
		}
		LOGGER.info(String.format("Theme '%s' created.", THEME.getClass().getSimpleName()));
		EventQueue.invokeLater(() -> PacManApp.THEME.snd_allSounds()); // preload all sounds on EDT
	}

	@Override
	public void init() {
		setController(new GameController());
	}
}