package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.Worlds;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class FollowMouseTestApp extends Application {

	public static void main(String[] args) {
		launch(FollowMouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Blinky Follows Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestUI(Worlds.arcade()));
	}
}

class FollowMouseTestUI extends PlayView {

	private Tile mousePosition = Tile.at(0, 0);

	public FollowMouseTestUI(PacManWorld world) {
		super(world, new Game(world, 1), new ArcadeTheme());
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		world.takePart(world.blinky);
		world.blinky.behavior(CHASING, world.blinky.isHeadingFor(() -> mousePosition));
		world.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		if (Mouse.moved()) {
			mousePosition = Tile.at(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
		}
		world.blinky.update();
	}
}