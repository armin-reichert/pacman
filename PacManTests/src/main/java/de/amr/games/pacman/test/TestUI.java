package de.amr.games.pacman.test;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.SpeedLimits.ghostSpeedLimit;
import static de.amr.games.pacman.controller.SpeedLimits.pacManSpeedLimit;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.DefaultPopulation;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.play.SimplePlayView.RenderingStyle;
import de.amr.games.pacman.view.theme.ArcadeTheme;
import de.amr.games.pacman.view.theme.Theme;

public class TestUI implements Lifecycle, VisualController {

	protected final World world;
	protected final Game game;
	protected final Population people;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final Theme theme;
	protected PlayView view;

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return world.population().ghosts().filter(world::included);
	}

	protected void include(Creature<?>... creatures) {
		Stream.of(creatures).forEach(world::include);
	}

	public TestUI() {
		world = Universe.arcadeWorld();
		world.clearFood();
		people = new DefaultPopulation();
		people.populate(world);

		pacMan = people.pacMan();
		blinky = people.blinky();
		pinky = people.pinky();
		inky = people.inky();
		clyde = people.clyde();
		
		theme = new ArcadeTheme();
		game = new Game(1, world.totalFoodCount());
		people.play(game);

		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		world.population().ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game)));

		view = new PlayView(world, theme, game, app().settings().width, app().settings().height);
		view.style = RenderingStyle.ARCADE;
		view.updateRenderers(world, theme);
		view.turnScoresOff();
		view.init();
	}
	
	@Override
	public void init() {
		world.population().creatures().forEach(Creature::init);
		world.population().creatures().forEach(creature -> world.exclude(creature));
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce("z")) {
			view.style = view.style == RenderingStyle.ARCADE ? RenderingStyle.BLOCK : RenderingStyle.ARCADE;
			view.updateRenderers(world, theme);
		}
		if (Keyboard.keyPressedOnce("g")) {
			if (view.isShowingGrid()) {
				view.turnGridOff();
			} else {
				view.turnGridOn();
			}
		}
		if (Keyboard.keyPressedOnce("r")) {
			if (view.isShowingRoutes()) {
				view.turnRoutesOff();
			} else {
				view.turnRoutesOn();
			}
		}
		world.population().creatures().filter(world::included).forEach(Creature::update);
	}
}