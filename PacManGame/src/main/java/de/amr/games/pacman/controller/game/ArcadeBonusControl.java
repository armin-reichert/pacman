package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.game.BonusState.INACTIVE;
import static de.amr.games.pacman.controller.game.BonusState.CONSUMED;
import static de.amr.games.pacman.controller.game.BonusState.CONSUMABLE;
import static de.amr.games.pacman.controller.game.GameController.sec;

import java.util.Random;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus food (fruits, symbols) appear at a dedicated position for around 9 seconds. When consumed,
 * the bonus is displayed for 3 seconds as a number representing its value and then it disappears.
 * 
 * @author Armin Reichert
 */
public class ArcadeBonusControl extends StateMachine<BonusState, PacManGameEvent> {

	public ArcadeBonusControl(Game game, World world) {
		super(BonusState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("Bonus Controller")
			.initialState(INACTIVE)
			.states()
			
				.state(INACTIVE)
					.onEntry(world::hideTemporaryFood)
			
				.state(CONSUMABLE)
					.timeoutAfter(() -> sec(Game.BONUS_SECONDS + new Random().nextFloat()))
					.onEntry(() -> {
							ArcadeBonus bonus = ArcadeBonus.valueOf(game.level.bonusSymbol);
							bonus.setValue(game.level.bonusValue);
							world.showTemporaryFood(bonus, ArcadeWorld.BONUS_LOCATION);
							loginfo("Bonus %s activated for %.2f sec", bonus, state().getDuration() / 60f);
					})
				
				.state(CONSUMED).timeoutAfter(sec(3))

			.transitions()
				
				.when(CONSUMABLE).then(CONSUMED).on(BonusFoundEvent.class)
					.act(() -> {
						world.temporaryFood().ifPresent(food -> {
							food.consume();
							loginfo("Bonus %s consumed after %.2f sec",	food, state().getTicksConsumed() / 60f);
						});
					})
					
				.when(CONSUMABLE).then(INACTIVE).onTimeout()
					.act(() -> {
						world.temporaryFood().ifPresent(food -> {
							food.consume();
							loginfo("Bonus %s has not been consumed", food);
						});
					})
				
				.when(CONSUMED).then(INACTIVE).onTimeout()
		
		.endStateMachine();
		/*@formatter:on*/
		init();
	}
}