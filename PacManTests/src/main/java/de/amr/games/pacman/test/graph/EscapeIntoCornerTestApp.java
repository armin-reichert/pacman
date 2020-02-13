package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.PacManState.EATING;

import java.util.Optional;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.core.MovingActor;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(EscapeIntoCornerTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		Game game = new Game();
		Cast cast = new Cast(game);
		Theme theme = new ArcadeTheme();
		setController(new EscapeIntoCornerTestUI(cast, theme));
	}
}

class EscapeIntoCornerTestUI extends PlayView implements VisualController {

	public EscapeIntoCornerTestUI(Cast cast, Theme theme) {
		super(cast, theme);
		showRoutes = () -> true;
		showStates = () -> true;
		showScores = () -> false;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		cast.pacMan.setActing(true);
		cast.pacMan.init();
		cast.pacMan.setState(EATING);
		cast.blinky.setActing(true);
		cast.blinky.behavior(FRIGHTENED, cast.blinky.isFleeingToSafeCorner(cast.pacMan));
		cast.blinky.init();
		cast.blinky.setState(FRIGHTENED);
	}

	@Override
	public void update() {
		super.update();
		cast.movingActorsOnStage().forEach(MovingActor::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}