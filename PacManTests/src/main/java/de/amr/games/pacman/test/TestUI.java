package de.amr.games.pacman.test;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.SpeedLimits.ghostSpeedLimit;
import static de.amr.games.pacman.controller.SpeedLimits.pacManSpeedLimit;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
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
import de.amr.games.pacman.view.render.GhostRenderer;
import de.amr.games.pacman.view.render.PacManRenderer;
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
		pacMan = people.pacMan();
		blinky = people.blinky();
		pinky = people.pinky();
		inky = people.inky();
		clyde = people.clyde();
		people.populate(world);

		theme = new ArcadeTheme();
		pacMan.setRenderer(new PacManRenderer(pacMan, theme));
		people.ghosts().forEach(ghost -> ghost.setRenderer(new GhostRenderer(ghost, theme)));
		
		game = new Game(1, world.totalFoodCount());
		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		world.population().ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game)));
		people.play(game);
	}

	@Override
	public void init() {
		view = new PlayView(world, theme, game, app().settings().width, app().settings().height);
		view.init();
	}

	@Override
	public void update() {
		world.population().creatures().filter(world::included).forEach(Creature::update);
	}
}