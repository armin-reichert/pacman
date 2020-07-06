package de.amr.games.pacman.controller.actor;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.core.BonusState.ACTIVE;
import static de.amr.games.pacman.model.world.core.BonusState.CONSUMED;
import static de.amr.games.pacman.model.world.core.BonusState.INACTIVE;

import java.util.Random;

import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Bonus;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus symbols (fruit or other symbol) appear at the bonus position for around 9 seconds. When
 * consumed, the bonus is displayed for 3 seconds as a number representing its value and then
 * disappears.
 * 
 * @author Armin Reichert
 */
public class BonusControl extends StateMachine<BonusState, PacManGameEvent> {

	public BonusControl(Game game, World world) {
		super(BonusState.class);
		/*@formatter:off*/
		beginStateMachine()
			.description(String.format("[%s]", "Bonus"))
			.initialState(INACTIVE)
			.states()
				.state(INACTIVE)
					.onEntry(() -> {
						world.setBonus(null);
					})
				.state(ACTIVE)
					.timeoutAfter(() -> sec(9 + new Random().nextFloat()))
					.onEntry(() -> {
						Bonus bonus = new Bonus(game.level.bonusSymbol.name(), game.level.bonusValue);
						bonus.state = ACTIVE;
						world.setBonus(bonus);
					})
				.state(CONSUMED)
					.timeoutAfter(() -> sec(3))
					.onEntry(() -> {
						world.getBonus().ifPresent(bonus -> {
							bonus.state = CONSUMED;
						});
					})
			.transitions()
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
				.when(ACTIVE).then(INACTIVE).onTimeout()
				.when(CONSUMED).then(INACTIVE).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		init();
	}

	public void activateBonus() {
		setState(ACTIVE);
		loginfo("Bonus %s activated, time: %.2f sec", this, state().getDuration() / 60f);
	}

	public void deactivateBonus() {
		init();
	}
}