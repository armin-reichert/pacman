package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.controller.actor.GhostSanity.ELROY1;
import static de.amr.games.pacman.controller.actor.GhostSanity.ELROY2;
import static de.amr.games.pacman.controller.actor.GhostSanity.INFECTABLE;

import de.amr.games.pacman.model.game.Game;
import de.amr.statemachine.core.StateMachine;

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