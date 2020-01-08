package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.Timing.sec;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.core.AbstractMazeResident;
import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Symbol;
import de.amr.games.pacman.theme.Theme;
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
		this.cast = cast;
		symbol = cast.game().level().bonusSymbol;
		value = cast.game().level().bonusValue;
		brain = new FsmComponent<>(buildFsm());
		brain.fsm().setLogger(Game.FSM_LOGGER);
		dress();
	}

	public void dress() {
		sprites.set("symbol", theme().spr_bonusSymbol(symbol));
		sprites.set("value", theme().spr_number(value));
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public Theme theme() {
		return theme();
	}

	public Symbol symbol() {
		return symbol;
	}

	public int value() {
		return value;
	}

	@Override
	public Maze maze() {
		return cast.game().maze();
	}

	@Override
	public FsmComponent<BonusState, PacManGameEvent> fsmComponent() {
		return brain;
	}

	public StateMachine<BonusState, PacManGameEvent> buildFsm() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description("[Bonus]")
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
				Vector2f center = tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	@Override
	public String toString() {
		return String.format("(%s,%d)", symbol, value);
	}
}