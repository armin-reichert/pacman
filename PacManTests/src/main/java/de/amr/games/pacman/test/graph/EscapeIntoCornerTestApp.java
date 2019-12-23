package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.behavior.Steerings.isFleeingToSafeCornerFrom;

import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(new EscapeIntoCornerTestApp(), args);
	}

	public EscapeIntoCornerTestApp() {
		settings.title = "Escape Into Corner";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast ensemble = new PacManGameCast(game, theme);
		setController(new EscapeIntoCornerTestUI(game, ensemble));
	}
}

class EscapeIntoCornerTestUI extends PlayView implements VisualController {

	public EscapeIntoCornerTestUI(PacManGame game, PacManGameCast cast) {
		super(cast);
		showRoutes(true);
		showStates(true);
		setShowScores(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().putOnStage(cast().pacMan);
		cast().pacMan.init();
		cast().putOnStage(cast().blinky);
		cast().blinky.during(FRIGHTENED, isFleeingToSafeCornerFrom(cast().pacMan));
		cast().blinky.init();
		cast().blinky.setState(FRIGHTENED);
	}

	@Override
	public void update() {
		super.update();
		cast().actorsOnStage().forEach(MazeResident::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}