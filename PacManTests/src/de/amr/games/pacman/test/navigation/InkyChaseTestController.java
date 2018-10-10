package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.PacManGame.TS;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.ActorNavigation;
import de.amr.games.pacman.navigation.ActorNavigationSystem;
import de.amr.games.pacman.view.play.PlayViewX;

public class InkyChaseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;

	public InkyChaseTestController() {
		game = new PacManGame();
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.setActive(game.getPacMan(), true);
		game.getPacMan().init();
		game.setActive(game.getPinky(), false);
		game.setActive(game.getClyde(), false);
		int width = app().settings.width;
		int height = app().settings.height;
		game.getInky().setMoveBehavior(GhostState.CHASING, attackWithBlinky(width, height));
		game.getActiveGhosts().forEach(ghost -> {
			ghost.init();
			ghost.setState(GhostState.CHASING);
		});
	}

	private ActorNavigation<Ghost> attackWithBlinky(int w, int h) {
		return game.getInky().headFor(() -> {
			Vector2f b = game.getBlinky().tf.getCenter();
			Tile strut = game.getPacMan().ahead(2);
			Vector2f p = Vector2f.of(strut.col * TS + TS / 2, strut.row * TS + TS / 2);
			Vector2f s = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
			LOGGER.info(String.format("Target point is (%.2f | %.2f)", s.x, s.y));
			Tile targetTile = tileFromVector(s.x, s.y);
			LOGGER.info(String.format("Target tile is %s", targetTile));
			return targetTile;
		});
	}

	private Tile tileFromVector(float sx, float sy) {
		if (sx > 0) {
			sx--;
		}
		if (sx > 0) {
			sy--;
		}
		return new Tile((int) sx / TS, (int) sy / TS);
	}

	@Override
	public void update() {
		game.getPacMan().update();
		game.getActiveGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}