package de.amr.games.pacman.actor.fsm;

import static de.amr.easy.game.Application.LOGGER;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * Prototypical implementation of the {@link FsmControlled} interface which can
 * be used as a delegate by an entity class.
 * <p>
 * When an entity cannot inherit directly from the {@link StateMachine} class,
 * it can implement the {@link FsmContainer} interface which delegates to an
 * instance of this class.
 * 
 * @author Armin Reichert
 *
 * @param <S> state (label) type of the FSM
 * 
 * @see {@link PacMan}, {@link Ghost}
 */
public class FsmComponent<S> implements FsmControlled<S> {

	public final String name;
	public final StateMachine<S, PacManGameEvent> fsm;
	public final Set<Consumer<PacManGameEvent>> listeners;
	public Predicate<PacManGameEvent> publishedEventIsLogged;

	public FsmComponent(String name, StateMachine<S, PacManGameEvent> fsm) {
		this.name = name;
		this.fsm = fsm;
		publishedEventIsLogged = event -> true;
		listeners = new LinkedHashSet<>();
	}

	@Override
	public StateMachine<S, PacManGameEvent> fsm() {
		return fsm;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void addGameEventListener(Consumer<PacManGameEvent> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		listeners.remove(listener);
	}

	public void publish(PacManGameEvent event) {
		if (publishedEventIsLogged.test(event)) {
			LOGGER.info(() -> String.format("'%s' published event '%s'", name, event));
		}
		listeners.forEach(listener -> listener.accept(event));
	}

	@Override
	public S getState() {
		return fsm.getState();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean is(S... states) {
		return fsm.is(states);
	}

	@Override
	public void setState(S state) {
		fsm.setState(state);
	}

	@Override
	public State<S, PacManGameEvent> state() {
		return fsm.state();
	}

	@Override
	public void process(PacManGameEvent event) {
		fsm.process(event);
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}
}