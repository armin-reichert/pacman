/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.bonus;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacmanfsm.controller.bonus.BonusFoodState.BONUS_CONSUMABLE;
import static de.amr.games.pacmanfsm.controller.bonus.BonusFoodState.BONUS_CONSUMED;
import static de.amr.games.pacmanfsm.controller.bonus.BonusFoodState.BONUS_INACTIVE;
import static de.amr.games.pacmanfsm.controller.game.Timing.sec;

import java.util.Random;
import java.util.function.Supplier;

import de.amr.games.pacmanfsm.controller.event.BonusFoundEvent;
import de.amr.games.pacmanfsm.controller.event.PacManGameEvent;
import de.amr.games.pacmanfsm.model.world.api.TemporaryFood;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls appearance and timing of bonus food.
 * 
 * @author Armin Reichert
 */
public class BonusFoodController extends StateMachine<BonusFoodState, PacManGameEvent> {

	private Random rnd = new Random();

	public BonusFoodController(TiledWorld world, Supplier<TemporaryFood> fnBonusSupplier) {
		super(BonusFoodState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("Bonus Food Controller")
			.initialState(BONUS_INACTIVE)
			.states()
			
				.state(BONUS_INACTIVE)
					.onEntry(world::hideTemporaryFood)
			
				.state(BONUS_CONSUMABLE)
					.timeoutAfter(this::bonusTime)
					.onEntry(() -> activateBonus(world, fnBonusSupplier.get()))
				
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

	private long bonusTime() {
		return sec(9 + rnd.nextFloat());
	}

	private void activateBonus(TiledWorld world, TemporaryFood bonus) {
		world.showTemporaryFood(bonus);
		loginfo("Bonus %s activated for %.2f sec", bonus, state().getDuration() / 60f);
	}

	private void consumeBonus(TiledWorld world) {
		world.temporaryFood().ifPresent(food -> {
			food.consume();
			loginfo("Bonus %s consumed after %.2f sec", food, state().getTicksConsumed() / 60f);
		});
	}

	private void deactivateBonus(TiledWorld world) {
		world.temporaryFood().ifPresent(food -> {
			food.deactivate();
			loginfo("Bonus %s has not been consumed and gets deactivated", food);
		});
	}
}