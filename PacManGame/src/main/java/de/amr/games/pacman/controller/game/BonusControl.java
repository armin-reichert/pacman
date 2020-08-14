package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.model.world.api.BonusFoodState.ABSENT;
import static de.amr.games.pacman.model.world.api.BonusFoodState.CONSUMED;
import static de.amr.games.pacman.model.world.api.BonusFoodState.PRESENT;

import java.util.Random;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.BonusFoodState;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus food (fruits, symbols) appear at a dedicated position for around 9 seconds. When consumed,
 * the bonus is displayed for 3 seconds as a number representing its value and then it disappears.
 * 
 * @author Armin Reichert
 */
public class BonusControl extends StateMachine<BonusFoodState, PacManGameEvent> {

	public BonusControl(Game game, World world) {
		super(BonusFoodState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("Bonus Controller")
			.initialState(ABSENT)
			.states()
			
				.state(ABSENT)
					.onEntry(world::clearBonusFood)
			
				.state(PRESENT)
					.timeoutAfter(() -> GameController.sec(Game.BONUS_SECONDS + new Random().nextFloat()))
					.onEntry(() -> {
							ArcadeBonus bonus = new ArcadeBonus(game.level.bonusSymbol);
							bonus.setValue(game.level.bonusValue);
							bonus.setState(PRESENT);
							world.addBonusFood(bonus);
							loginfo("Bonus '%s' activated for %.2f sec", bonus, state().getDuration() / 60f);
					})
				
				.state(CONSUMED).timeoutAfter(GameController.sec(3))

			.transitions()
				
				.when(PRESENT).then(CONSUMED).on(BonusFoundEvent.class)
					.act(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							bonusFood.setState(CONSUMED);
							loginfo("Bonus '%s' consumed after %.2f sec",	bonusFood, state().getTicksConsumed() / 60f);
						});
					})
					
				.when(PRESENT).then(ABSENT).onTimeout()
					.act(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							bonusFood.setState(CONSUMED);
							loginfo("Bonus '%s' not consumed", bonusFood);
						});
					})
				
				.when(CONSUMED).then(ABSENT).onTimeout()
		
		.endStateMachine();
		/*@formatter:on*/
		init();
	}
}