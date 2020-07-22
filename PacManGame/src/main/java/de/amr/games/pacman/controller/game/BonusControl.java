package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.components.BonusState.ACTIVE;
import static de.amr.games.pacman.model.world.components.BonusState.CONSUMED;
import static de.amr.games.pacman.model.world.components.BonusState.INACTIVE;

import java.util.Random;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bonus;
import de.amr.games.pacman.model.world.components.BonusState;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus symbols (fruit or other symbol) appear at the bonus position for around 9 seconds. When
 * consumed, the bonus is displayed for 3 seconds as a number representing its value and then it
 * disappears.
 * 
 * @author Armin Reichert
 */
public class BonusControl extends StateMachine<BonusState, PacManGameEvent> {

	public BonusControl(Game game, World world) {
		super(BonusState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description("BonusControl")
			.initialState(INACTIVE)
			.states()
				.state(INACTIVE)
					.onEntry(() -> world.setBonus(null))
				.state(ACTIVE)
					.timeoutAfter(() -> sec(Game.BONUS_SECONDS + new Random().nextFloat()))
					.onEntry(() -> {
						Bonus bonus = new Bonus(Game.BONUS_LOCATION, game.level.bonusSymbol.name(), game.level.bonusValue);
						bonus.state = ACTIVE;
						world.setBonus(bonus);
						loginfo("Bonus '%s' (value %d) activated for %.2f sec", 
								bonus.symbol, bonus.value, state().getDuration() / 60f);
					})
				.state(CONSUMED)
					.timeoutAfter(() -> sec(3))
					.onEntry(() -> {
						Bonus bonus = world.getBonus().get();
						bonus.state = CONSUMED;
						loginfo("Bonus '%s' (value %d) consumed after %.2f sec", 
								bonus.symbol, bonus.value, state(ACTIVE).getTicksConsumed() / 60f);
					})
			.transitions()
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
				.when(ACTIVE).then(INACTIVE).onTimeout()
				.when(CONSUMED).then(INACTIVE).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
		init();
	}
}