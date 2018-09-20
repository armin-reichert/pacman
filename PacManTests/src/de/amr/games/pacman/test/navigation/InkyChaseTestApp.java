package de.amr.games.pacman.test.navigation;

import java.util.concurrent.Executors;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class InkyChaseTestApp extends PacManApp {

	public static void main(String[] args) {
		try {
			THEME = ClassicPacManTheme.class.newInstance();
			LOGGER.info(String.format("Theme '%s' created.", THEME.getClass().getSimpleName()));
			Executors.newSingleThreadExecutor().submit((() -> THEME.snd_music_all()));
			launch(new InkyChaseTestApp());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public InkyChaseTestApp() {
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestController());
	}
}