package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostMadness.ELROY1;
import static de.amr.games.pacman.controller.creatures.ghost.GhostMadness.ELROY2;
import static de.amr.games.pacman.controller.creatures.ghost.GhostMadness.HEALTHY;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.statemachine.core.StateMachine;

/**
 * When Blinky takes part in a game, he becomes mentally ill (transforms into a "Cruise Elroy")
 * whenever the number of remaining food reaches certain numbers which depend on the game level.
 * This state machine controls the mental state of Blinky.
 * 
 * @author Armin Reichert
 */
public class MadnessController extends StateMachine<GhostMadness, Void> {

	private boolean elroySuspended;

	public MadnessController(Game game, World world, Ghost ghost, PacMan pacMan) {
		super(GhostMadness.class);
		//@formatter:off
		beginStateMachine()
			.initialState(HEALTHY)
			.description(() -> String.format("Ghost %s Madness", ghost.name))
			.states()
				.state(HEALTHY)
					.onEntry(() -> {
						you(ghost).when(SCATTERING).headFor().tile(world.width() - 3, 0).ok();
					})
				
				.state(ELROY1)
					.onEntry(() -> {
						you(ghost).when(SCATTERING).headFor().tile(pacMan::tileLocation).ok();
					})
					
				.state(ELROY2)
					.onEntry(() -> {
						you(ghost).when(SCATTERING).headFor().tile(pacMan::tileLocation).ok();
					})
			
			.transitions()
			
				.when(ELROY1).then(ELROY2)
					.condition(() -> game.level.remainingFoodCount() <= game.level.elroy2DotsLeft)
					.annotation(() -> String.format("Remaining pellets <= %d", game.level.elroy2DotsLeft))
			
				.when(HEALTHY).then(ELROY2)
					.condition(() -> !elroySuspended && game.level.remainingFoodCount() <= game.level.elroy2DotsLeft)
					.annotation(() -> String.format("Remaining pellets <= %d", game.level.elroy2DotsLeft))
					
				.when(HEALTHY).then(ELROY1)
					.condition(() -> !elroySuspended && game.level.remainingFoodCount() <= game.level.elroy1DotsLeft)
					.annotation(() -> String.format("Remaining pellets <= %d", game.level.elroy1DotsLeft))
				
		.endStateMachine();
		//@formatter:on
	}

	public void suspendElroyState() {
		if (getState() == ELROY1 || getState() == ELROY2) {
			setState(HEALTHY);
			elroySuspended = true;
		}
	}

	public void resumeElroyState(Game game) {
		if (getState() == HEALTHY) {
			if (game.level.remainingFoodCount() <= game.level.elroy1DotsLeft) {
				if (game.level.remainingFoodCount() <= game.level.elroy2DotsLeft) {
					setState(ELROY2);
				} else {
					setState(ELROY1);
				}
			}
			elroySuspended = false;
		}
	}

	public boolean isElroySuspended() {
		return elroySuspended;
	}
}