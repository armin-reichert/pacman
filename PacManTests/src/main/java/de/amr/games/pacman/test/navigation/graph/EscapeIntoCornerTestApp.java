package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.PacManState.EATING;

import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(EscapeIntoCornerTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestUI());
	}
}

class EscapeIntoCornerTestUI extends PlayView implements VisualController {

	public EscapeIntoCornerTestUI() {
		super(new Game(), new ArcadeTheme());
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.world.eatFood();
		game.takePart(game.pacMan);
		game.pacMan.setState(EATING);
		game.takePart(game.blinky);
		game.blinky.behavior(FRIGHTENED, game.blinky.isFleeingToSafeCorner(game.pacMan));
		game.blinky.setState(FRIGHTENED);
	}

	@Override
	public void update() {
		super.update();
		game.creaturesOnStage().forEach(Creature::update);
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}
}