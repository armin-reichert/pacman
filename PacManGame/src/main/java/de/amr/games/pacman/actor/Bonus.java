package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.Timing.sec;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.core.AbstractMazeResident;
import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Symbol;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position
 * for around 9 seconds. When consumed, the bonus is displayed for 3 seconds as
 * a number representing its value and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends AbstractMazeResident implements Actor<BonusState> {

	private final SpriteMap sprites = new SpriteMap();
	private final Cast cast;
	private final FsmComponent<BonusState, PacManGameEvent> brain;
	private final Symbol symbol;
	private final int value;

	public Bonus(Cast cast) {
		super("Bonus");
		this.cast = cast;
		placeHalfRightOf(maze().bonusTile);
		symbol = game().level().bonusSymbol;
		value = game().level().bonusValue;
		brain = new FsmComponent<>(buildFsm());
		brain.fsm().setLogger(Game.FSM_LOGGER);
		sprites.set("symbol", cast.theme().spr_bonusSymbol(symbol));
		sprites.set("value", cast.theme().spr_number(value));
	}

	public Symbol symbol() {
		return symbol;
	}

	public int value() {
		return value;
	}

	@Override
	public Cast cast() {
		return cast;
	}
	
	@Override
	public Maze maze() {
		return cast.maze();
	}

	@Override
	public FsmComponent<BonusState, PacManGameEvent> fsmComponent() {
		return brain;
	}

	@Override
	public StateMachine<BonusState, PacManGameEvent> buildFsm() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description(String.format("[%s]", name()))
			.initialState(ACTIVE)
			.states()
				.state(ACTIVE)
					.timeoutAfter(() -> sec(9 + new Random().nextFloat()))
					.onEntry(() -> sprites.select("symbol"))
				.state(CONSUMED)
					.timeoutAfter(sec(3))
					.onEntry(() -> sprites.select("value"))
				.state(INACTIVE)
					.onEntry(cast::removeBonus)
			.transitions()
				.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
				.when(ACTIVE).then(INACTIVE).onTimeout()
				.when(CONSUMED).then(INACTIVE).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void init() {
		brain.init();
	}

	@Override
	public void update() {
		brain.update();
	}

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			sprites.current().ifPresent(sprite -> {
				float x = tf.getCenter().x - sprite.getWidth() / 2;
				float y = tf.getCenter().y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	@Override
	public String toString() {
		return String.format("(%s,%d)", symbol, value);
	}
}