package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steerings;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.core.Pen;
import de.amr.games.pacman.view.play.PlayView;

public class FollowTargetTilesTestApp extends Application {

	public static void main(String[] args) {
		launch(new FollowTargetTilesTestApp(), args);
	}

	public FollowTargetTilesTestApp() {
		settings.title = "Follow Target Tiles";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new FollowTargetTilesTestUI(cast));
	}
}

class FollowTargetTilesTestUI extends PlayView implements VisualController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(false);
		setShowScores(false);
		setShowGrid(true);
		targets = Arrays.asList(maze().cornerNW, maze().ghostHouseSeats[0], maze().cornerNE, maze().cornerSE,
				maze().pacManHome, maze().cornerSW);
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
		current = 0;
		game().init();
		maze().removeFood();
		theme().snd_ghost_chase().volume(0);
		cast().putOnStage(cast().blinky);
		cast().blinky.during(CHASING, Steerings.isHeadingFor(() -> targets.get(current)));
		cast().blinky.placeAtTile(targets.get(0), 0, 0);
		cast().blinky.setState(CHASING);
		cast().blinky.setEnteredNewTile();
	}

	@Override
	public void update() {
		if (cast().blinky.tile() == targets.get(current)) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
				game().enterLevel(game().level.number + 1);
				maze().removeFood();
			}
		}
		cast().blinky.update();
		super.update();
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		Pen pen = new Pen(g);
		pen.color(Color.YELLOW);
		pen.fontSize(8);
		for (int i = 0; i < targets.size(); ++i) {
			Tile target = targets.get(i);
			pen.draw("" + i, target.col, target.row);
		}
	}
}