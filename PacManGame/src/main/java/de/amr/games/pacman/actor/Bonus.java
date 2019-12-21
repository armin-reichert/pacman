package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.Timing.sec;
import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.games.pacman.actor.core.AbstractMazeResident;
import de.amr.games.pacman.actor.fsm.FsmComponent;
import de.amr.games.pacman.actor.fsm.FsmContainer;
import de.amr.games.pacman.actor.fsm.FsmControlled;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position
 * for around 9 seconds. When consumed, the bonus is displayed for 3 seconds as
 * a number representing its value and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends AbstractMazeResident implements FsmContainer<BonusState, PacManGameEvent> {

	public final PacManGameCast cast;
	public final FsmComponent<BonusState, PacManGameEvent> fsmComponent;
	public final BonusSymbol symbol;
	public final int value;

	public Bonus(PacManGameCast cast) {
		super("Bonus");
		this.cast = cast;
		fsmComponent = buildFsmComponent(name());
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
		placeAtTile(cast.game.maze.bonusTile, Tile.SIZE / 2, 0);
		symbol = cast.game.level.bonusSymbol;
		value = cast.game.level.bonusValue;
		sprites.set("symbol", cast.theme().spr_bonusSymbol(symbol));
		sprites.set("number", cast.theme().spr_pinkNumber(binarySearch(PacManGame.POINTS_BONUS, value)));
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
	}

	@Override
	public FsmControlled<BonusState, PacManGameEvent> fsmComponent() {
		return fsmComponent;
	}

	private FsmComponent<BonusState, PacManGameEvent> buildFsmComponent(String name) {
		StateMachine<BonusState, PacManGameEvent> fsm = buildStateMachine(name);
		fsm.traceTo(PacManGame.FSM_LOGGER, () -> 60);
		return new FsmComponent<>(name, fsm);
	}

	private StateMachine<BonusState, PacManGameEvent> buildStateMachine(String name) {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(BonusState.class, PacManGameEvent.class)
			.description(String.format("[%s]", name))
			.initialState(ACTIVE)
			.states()
				.state(ACTIVE)
					.timeoutAfter(() -> sec(9 + new Random().nextFloat()))
					.onEntry(() -> sprites.select("symbol"))
				.state(CONSUMED)
					.timeoutAfter(sec(3))
					.onEntry(() -> sprites.select("number"))
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
		super.init();
		fsmComponent.init();
	}

	@Override
	public void update() {
		super.update();
		fsmComponent.update();
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			float dx = centerX() - sprite.getWidth() / 2;
			float dy = centerY() - sprite.getHeight() / 2;
			g.translate(dx, dy);
			sprite.draw(g);
			g.translate(-dx, -dy);
		});
	}

	@Override
	public String toString() {
		return String.format("(%s,%d)", symbol, value);
	}
}