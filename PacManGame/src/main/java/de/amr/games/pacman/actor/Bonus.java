package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.logging.Logger;

import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.statemachine.StateMachine;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position
 * for around 9 seconds. When consumed, the bonus is displayed for 3 seconds as
 * a number representing its value and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends MazeResident implements Actor<BonusState> {

	public final PacManGameCast cast;
	public BonusSymbol symbol;
	public int value;

	private final ActorPrototype<BonusState> _actor;

	public Bonus(PacManGameCast cast) {
		super(cast.game.maze);
		this.cast = cast;
		_actor = new ActorPrototype<>("Bonus", buildStateMachine());
		_actor.fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
	}

	private StateMachine<BonusState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description("[Bonus]")
			.initialState(ACTIVE)
			.states()
				.state(ACTIVE)
					.timeoutAfter(cast.game.level::bonusActiveTicks)
					.onEntry(() -> {
						symbol = cast.game.level.bonusSymbol;
						value = cast.game.level.bonusValue;
						sprites.set("symbol", cast.theme.spr_bonusSymbol(symbol));
						sprites.set("number", cast.theme.spr_pinkNumber(Arrays.binarySearch(PacManGame.BONUS_NUMBERS, value)));
						sprites.select("symbol");
						placeAtTile(cast.game.maze.bonusTile, Maze.TS / 2, 0);
						activate();
					})
				.state(CONSUMED)
					.timeoutAfter(cast.game.level::bonusConsumedTicks)
					.onEntry(() -> {
						sprites.select("number");
					})
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
	public Actor<BonusState> _actor() {
		return _actor;
	}

	@Override
	public void init() {
		super.init();
		_actor.init();
	}

	@Override
	public void update() {
		super.update();
		_actor.update();
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			// center sprite over collision box
			float dx = tf.getX() + tf.getWidth() / 2 - sprite.getWidth() / 2;
			float dy = tf.getY() + tf.getHeight() / 2 - sprite.getHeight() / 2;
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