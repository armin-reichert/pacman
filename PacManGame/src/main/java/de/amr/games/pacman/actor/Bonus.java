package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.PacManGame;
import de.amr.statemachine.StateMachine;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position for around 9
 * seconds. When consumed, the bonus is displayed for 3 seconds as a number representing its value
 * and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Actor<BonusState> {

	public final BonusSymbol symbol;
	public final int value;
	public final int activeTime;
	public final int consumedTime;

	public Bonus(PacManGameCast cast, int activeTime, int consumedTime) {
		super("Bonus", cast);
		tf.setWidth(TS);
		tf.setHeight(TS);
		this.symbol = cast.game.level.bonusSymbol;
		this.value = cast.game.level.bonusValue;
		this.activeTime = activeTime;
		this.consumedTime = consumedTime;
		fsm = buildStateMachine();
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	private StateMachine<BonusState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description("[Bonus]")
			.initialState(ACTIVE)
			.states()
				.state(ACTIVE)
					.timeoutAfter(activeTime)
					.onEntry(() -> {
						sprites.set("symbol", cast.theme.spr_bonusSymbol(symbol));
						sprites.select("symbol");
					})
				.state(CONSUMED)
					.timeoutAfter(consumedTime)
					.onEntry(() -> {
						sprites.set("number", cast.theme.spr_pinkNumber(numberIndex(value)));
						sprites.select("number");
					})
				.state(INACTIVE)
					.onEntry(() -> cast.bonus = Optional.empty())
			.transitions()
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
				.when(ACTIVE).then(INACTIVE).onTimeout()
				.when(CONSUMED).then(INACTIVE).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	private int numberIndex(int value) {
		int index = Arrays.binarySearch(PacManGame.BONUS_NUMBERS, value);
		if (index >= 0) {
			return index;
		}
		throw new IllegalArgumentException("Illegal bonus value: " + value);
	}

	@Override
	protected void steer() {
		// does not move
	}

	@Override
	public float maxSpeed() {
		// does not move
		return 0;
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			// center sprite over collision box
			float dx = tf.getX() - (sprite.getWidth() - TS) / 2;
			float dy = tf.getY() - (sprite.getHeight() - TS) / 2;
			g.translate(dx, dy);
			sprite.draw(g);
			g.translate(-dx, -dy);
		});
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}