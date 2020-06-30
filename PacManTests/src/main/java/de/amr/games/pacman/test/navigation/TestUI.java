package de.amr.games.pacman.test.navigation;

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
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.model.world.Population;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;
import de.amr.games.pacman.view.theme.Theme;

public class TestUI implements Lifecycle, VisualController {

	protected final World world;
	protected final Game game;
	protected final Population aliens;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final PlayView view;
	protected final Theme theme;

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return world.population().ghosts().filter(world::isOnStage);
	}

	protected void putOnStage(Creature<?>... creatures) {
		Stream.of(creatures).forEach(creature -> world.putOnStage(creature, true));
	}

	public TestUI() {
		world = Universe.arcadeWorld();
		world.removeFood();
		game = new Game(1, world.totalFoodCount());

		aliens = new DefaultPopulation();
		aliens.populate(world);

		pacMan = aliens.pacMan();
		blinky = aliens.blinky();
		pinky = aliens.pinky();
		inky = aliens.inky();
		clyde = aliens.clyde();
		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		world.population().ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game)));

		aliens.play(game);

		theme = new ArcadeTheme();
		view = new PlayView(world, game, theme);
	}

	@Override
	public void init() {
		view.init();
	}

	@Override
	public void update() {
		world.population().creatures().filter(world::isOnStage).forEach(Creature::update);
	}
}