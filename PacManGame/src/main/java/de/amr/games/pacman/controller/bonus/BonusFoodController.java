package de.amr.games.pacman.controller.bonus;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.bonus.BonusFoodState.BONUS_CONSUMABLE;
import static de.amr.games.pacman.controller.bonus.BonusFoodState.BONUS_CONSUMED;
import static de.amr.games.pacman.controller.bonus.BonusFoodState.BONUS_INACTIVE;
import static de.amr.games.pacman.controller.game.GameController.sec;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls appearance and timing of bonus food.
 * 
 * @author Armin Reichert
 */
public abstract class BonusFoodController extends StateMachine<BonusFoodState, PacManGameEvent> {

	public BonusFoodController(Game game, World world) {
		super(BonusFoodState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("Bonus Food Controller")
			.initialState(BONUS_INACTIVE)
			.states()
			
				.state(BONUS_INACTIVE)
					.onEntry(world::hideTemporaryFood)
			
				.state(BONUS_CONSUMABLE)
					.timeoutAfter(this::activationTicks)
					.onEntry(() -> activateBonus(game, world))
				
				.state(BONUS_CONSUMED).timeoutAfter(sec(3))

			.transitions()
				
				.when(BONUS_CONSUMABLE).then(BONUS_CONSUMED).on(BonusFoundEvent.class)
					.act(() -> consumeBonus(world))
					
				.when(BONUS_CONSUMABLE).then(BONUS_INACTIVE).onTimeout()
					.act(() -> deactivateBonus(world))
				
				.when(BONUS_CONSUMED).then(BONUS_INACTIVE).onTimeout()
		
		.endStateMachine();
		/*@formatter:on*/
	}

	public abstract long activationTicks();

	public abstract void activateBonus(Game game, World world);

	public void consumeBonus(World world) {
		world.temporaryFood().ifPresent(food -> {
			food.consume();
			loginfo("Bonus %s consumed after %.2f sec", food, state().getTicksConsumed() / 60f);
		});
	}

	public void deactivateBonus(World world) {
		world.temporaryFood().ifPresent(food -> {
			food.deactivate();
			loginfo("Bonus %s has not been consumed and gets deactivated", food);
		});
	}
}