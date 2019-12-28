package de.amr.games.pacman.test.navigation;

import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.GhostHouseDoorMan;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class JumpingTestApp extends Application {

	public static void main(String[] args) {
		launch(new JumpingTestApp(), args);
	}

	public JumpingTestApp() {
		settings.title = "Jumping";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new JumpingTestUI(cast));
	}
}

class JumpingTestUI extends PlayView implements VisualController {

	public JumpingTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(false);
		showStates(true);
		showScores(false);
		showGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		ghostHouseDoorMan = new GhostHouseDoorMan(cast());
		ghostHouseDoorMan.closeDoor();
		cast().ghosts().forEach(cast()::setActorOnStage);
	}

	@Override
	public void update() {
		super.update();
		cast().ghostsOnStage().forEach(Ghost::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}