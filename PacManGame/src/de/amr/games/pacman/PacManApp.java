package de.amr.games.pacman;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreenMode;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Pac-Man game.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static PacManTheme theme;

	public static void main(String[] args) {
		LOGGER.setLevel(Level.INFO);
		try {
			theme = new ClassicPacManTheme();
			LOGGER.info(String.format("Theme '%s' created.", theme.getClass().getSimpleName()));
			LOGGER.info("Loading audio clips...");
			theme.snd_clips_all();
			LOGGER.info("Audio clips loaded.");
			runAsync(() -> {
				LOGGER.info("Loading background music...");
				theme.snd_music_all();
			}).thenAccept(result -> LOGGER.info("Music loaded."));
			launch(new PacManApp(), args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public PacManApp() {
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		settings.fullScreenMode = new FullScreenMode(800, 600, 32);
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		setController(new PacManGameController());
	}
}