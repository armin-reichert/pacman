package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.controller.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.controller.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.controller.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.Game.sec;

import java.util.Random;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.api.FsmContainer;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position for around 9
 * seconds. When consumed, the bonus is displayed for 3 seconds as a number representing its value
 * and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Entity implements FsmContainer<BonusState, PacManGameEvent> {

	public final Game game;
	public Symbol symbol;
	public int value;
	public final SpriteMap sprites = new SpriteMap();
	private final Fsm<BonusState, PacManGameEvent> brain;

	public Bonus(Game game) {
		this.game = game;
		tf.width = Tile.SIZE;
		tf.height = Tile.SIZE;
		brain = buildFsm();
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		init();
	}

	private StateMachine<BonusState, PacManGameEvent> buildFsm() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description(String.format("[%s]", "Bonus"))
			.initialState(INACTIVE)
			.states()
				.state(INACTIVE)
					.onEntry(() -> visible = false)
				.state(ACTIVE)
					.timeoutAfter(() -> sec(9 + new Random().nextFloat()))
					.onEntry(() -> {
						sprites.select("symbol");
						visible = true;
					})
				.state(CONSUMED)
					.timeoutAfter(sec(3))
					.onEntry(() -> sprites.select("value"))
			.transitions()
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
				.when(ACTIVE).then(INACTIVE).onTimeout()
				.when(CONSUMED).then(INACTIVE).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public Fsm<BonusState, PacManGameEvent> fsm() {
		return brain;
	}

	public void activate(Theme theme) {
		brain.setState(ACTIVE);
		symbol = game.level.bonusSymbol;
		value = game.level.bonusValue;
		sprites.set("symbol", theme.spr_bonusSymbol(symbol));
		sprites.set("value", theme.spr_number(value));
	}

	public void deactivate() {
		init();
	}

	@Override
	public String toString() {
		return String.format("(%s,%d)", symbol, value);
	}
}