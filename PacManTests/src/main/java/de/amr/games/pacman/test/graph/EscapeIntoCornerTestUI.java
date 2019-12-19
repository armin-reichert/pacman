package de.amr.games.pacman.test.graph;

import static de.amr.games.pacman.actor.behavior.Steerings.isFleeingToSafeCornerFrom;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestUI extends PlayView implements VisualController {

	public EscapeIntoCornerTestUI(PacManGame game, PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
	}

	@Override
	public void init() {
		super.init();
		game.init();
		game.maze.removeFood();
		cast.putOnStage(cast.pacMan);
		cast.pacMan.init();
		cast.putOnStage(cast.blinky);
		cast.blinky.during(GhostState.FRIGHTENED, isFleeingToSafeCornerFrom(cast.pacMan));
		cast.blinky.init();
		cast.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		cast.pacMan.update();
		cast.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}