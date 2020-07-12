package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostSanity.ELROY1;
import static de.amr.games.pacman.controller.creatures.ghost.GhostSanity.ELROY2;
import static de.amr.games.pacman.controller.creatures.ghost.GhostSanity.INFECTABLE;

import de.amr.games.pacman.model.game.Game;
import de.amr.statemachine.core.StateMachine;

/**
 * When Blinky takes part in a game, he becomes mentally ill (transforms into a "Cruise Elroy")
 * whenever the number of remaining food reaches certain numbers which depend on the game level.
 * This state machine controls the sanity state of Blinky.
 * 
 * @author Armin Reichert
 */
public class GhostSanityControl extends StateMachine<GhostSanity, Void> {

	public GhostSanityControl(Game game, String ghostName, GhostSanity initialSanity) {
		super(GhostSanity.class);
		//@formatter:off
		beginStateMachine()
			.initialState(initialSanity)
			.description(() -> String.format("[%s sanity]", ghostName))
			.states()
			.transitions()
			
				.when(INFECTABLE).then(ELROY2)
					.condition(() -> game.level.remainingFoodCount() <= game.level.elroy2DotsLeft)
					
				.when(INFECTABLE).then(ELROY1)
					.condition(() -> game.level.remainingFoodCount() <= game.level.elroy1DotsLeft)
				
				.when(ELROY1).then(ELROY2)
					.condition(() -> game.level.remainingFoodCount() <= game.level.elroy2DotsLeft)
					
		.endStateMachine();
		//@formatter:on
	}
}