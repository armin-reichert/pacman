package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.arcade.BonusState.ACTIVE;
import static de.amr.games.pacman.model.world.arcade.BonusState.CONSUMED;
import static de.amr.games.pacman.model.world.arcade.BonusState.INACTIVE;

import java.util.Random;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.arcade.BonusState;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus symbols (fruit or other symbol) appear at the bonus position for around 9 seconds. When
 * consumed, the bonus is displayed for 3 seconds as a number representing its value and then it
 * disappears.
 * 
 * @author Armin Reichert
 */
public class BonusControl extends StateMachine<BonusState, PacManGameEvent> {

	public BonusControl(Game game, ArcadeWorld world) {
		super(BonusState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("BonusControl")
			.initialState(INACTIVE)
			.states()
			
				.state(INACTIVE)
					.onEntry(() -> {
						world.setBonusState(INACTIVE);
						world.setBonusSymbol(null);
						world.setBonusValue(0);
					})
			
				.state(ACTIVE)
					.timeoutAfter(() -> sec(Game.BONUS_SECONDS + new Random().nextFloat()))
					.onEntry(() -> {
						world.setBonusState(ACTIVE);
						world.setBonusSymbol(game.level.bonusSymbol);
						world.setBonusValue(game.level.bonusValue);
						loginfo("Bonus '%s' (value %d) activated for %.2f sec",
								world.getBonusSymbol(), world.getBonusValue(), state().getDuration() / 60f);
					})
				
				.state(CONSUMED)
					.timeoutAfter(() -> sec(3))
					.onEntry(() -> {
						world.setBonusState(CONSUMED);
						world.setBonusSymbol(null);
					})

			.transitions()
				
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
					.act(() -> {
						loginfo("Bonus '%s' (value %d) consumed after %.2f sec", 
								world.getBonusSymbol(), game.level.bonusValue, state().getTicksConsumed() / 60f);
					})
					
				.when(ACTIVE).then(INACTIVE).onTimeout()
				
				.when(CONSUMED).then(INACTIVE).onTimeout()
		
		.endStateMachine();
		/*@formatter:on*/
		init();
	}
}