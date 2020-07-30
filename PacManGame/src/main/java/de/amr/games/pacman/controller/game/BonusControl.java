package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.BonusFoodState.ACTIVE;
import static de.amr.games.pacman.model.world.api.BonusFoodState.CONSUMED;
import static de.amr.games.pacman.model.world.api.BonusFoodState.INACTIVE;

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
			.description("BonusControl")
			.initialState(INACTIVE)
			.states()
			
				.state(INACTIVE)
					.onEntry(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							bonusFood.setState(INACTIVE);
						});
					})
			
				.state(ACTIVE)
					.timeoutAfter(() -> sec(Game.BONUS_SECONDS + new Random().nextFloat()))
					.onEntry(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							bonusFood.setState(ACTIVE);
							ArcadeBonus bonus = (ArcadeBonus) bonusFood;
							bonus.symbol = game.level.bonusSymbol;
							bonus.value = game.level.bonusValue;
							loginfo("Bonus '%s' (value %d) activated for %.2f sec",	
									bonus.symbol, bonus.value, state().getDuration() / 60f);
						});
					})
				
				.state(CONSUMED)
					.timeoutAfter(() -> sec(3))
					.onEntry(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							bonusFood.setState(CONSUMED);
						});
					})

			.transitions()
				
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
					.act(() -> {
						world.bonusFood().ifPresent(bonusFood -> {
							ArcadeBonus bonus = (ArcadeBonus) bonusFood;
							loginfo("Bonus '%s' (value %d) consumed after %.2f sec", 
									bonus.symbol, bonus.value, state().getTicksConsumed() / 60f);
						});
					})
					
				.when(ACTIVE).then(INACTIVE).onTimeout()
				
				.when(CONSUMED).then(INACTIVE).onTimeout()
		
		.endStateMachine();
		/*@formatter:on*/
		init();
	}
}