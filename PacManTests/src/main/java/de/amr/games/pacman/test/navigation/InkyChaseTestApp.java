package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class InkyChaseTestApp extends Application {

	public static void main(String[] args) {
		launch(InkyChaseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestUI(new Game(), new ArcadeTheme()));
	}
}

class InkyChaseTestUI extends PlayView {

	public InkyChaseTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		theme.snd_ghost_chase().volume(0);
		Stream.of(game.pacMan, game.inky, game.blinky).forEach(game.stage::add);
		game.ghostsOnStage().forEach(ghost -> {
			ghost.followState = CHASING;
		});
		message.text = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			game.pacMan.setState(PacManState.EATING);
			message.text = "";
		}
		game.pacMan.update();
		game.ghostsOnStage().forEach(Ghost::update);
		super.update();
	}
}