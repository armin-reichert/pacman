package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.BonusState.CONSUMED;
import static de.amr.games.pacman.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.model.Timing.sec;
import static java.util.Arrays.binarySearch;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.core.AbstractMazeResident;
import de.amr.games.pacman.actor.core.PacManGameActor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.StateMachine;

/**
 * Bonus symbol (fruit or other symbol) that appears at the maze bonus position for around 9
 * seconds. When consumed, the bonus is displayed for 3 seconds as a number representing its value
 * and then disappears.
 * 
 * @author Armin Reichert
 */
public class Bonus extends AbstractMazeResident implements PacManGameActor<BonusState> {

	private final SpriteMap sprites = new SpriteMap();
	private final PacManGameCast cast;
	private final FsmComponent<BonusState, PacManGameEvent> brain;
	public final BonusSymbol symbol;
	public final int value;

	public Bonus(PacManGameCast cast) {
		super("Bonus");
		this.cast = cast;
		tf.setHeight(Tile.SIZE);
		tf.setWidth(Tile.SIZE);
		placeHalfRightOf(maze().bonusTile);
		symbol = game().level().bonusSymbol;
		value = game().level().bonusValue;
		brain = buildBrain();
		brain.fsm().traceTo(PacManGame.FSM_LOGGER, () -> 60);
		sprites.set("symbol", cast.theme().spr_bonusSymbol(symbol));
		sprites.set("number", cast.theme().spr_pinkNumber(binarySearch(PacManGame.POINTS_BONUS, value)));
	}

	@Override
	public PacManGameCast cast() {
		return cast;
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
		brain.init();
	}

	@Override
	public void update() {
		brain.update();
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			float dx = tf.getCenter().x - sprite.getWidth() / 2;
			float dy = tf.getCenter().y - sprite.getHeight() / 2;
			sprite.draw(g, dx, dy);
		});
	}

	@Override
	public String toString() {
		return String.format("(%s,%d)", symbol, value);
	}
}