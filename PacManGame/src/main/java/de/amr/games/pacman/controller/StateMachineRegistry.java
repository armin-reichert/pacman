package de.amr.games.pacman.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.statemachine.core.StateMachine;

/**
 * A central place to register/unregister the used state machines.
 * 
 * @author Armin Reichert
 */
public final class StateMachineRegistry {

	public static final StateMachineRegistry IT = new StateMachineRegistry();

	private final Set<StateMachine<?, ?>> machines;
	private boolean shutUp;

	private StateMachineRegistry() {
		machines = new HashSet<>();
		shutUp = true;
	}

	public Collection<StateMachine<?, ?>> machines() {
		return Collections.unmodifiableSet(machines);
	}

	public <FSM extends StateMachine<?, ?>> void register(Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			this.machines.add(fsm);
			fsm.getTracer().shutUp(shutUp);
			Application.loginfo("State machine registered: %s", fsm);
		});
	}

	public <FSM extends StateMachine<?, ?>> void unregister(Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			this.machines.remove(fsm);
			fsm.getTracer().shutUp(false);
			Application.loginfo("State machine unregistered: %s", fsm);
		});
	}

	public void shutUp(boolean shutUp) {
		this.shutUp = shutUp;
		machines.stream().map(StateMachine::getTracer).forEach(tracer -> tracer.shutUp(shutUp));
		Application.loginfo("State machines are silent: %s", shutUp);
	}

	public boolean isKeepingItsMouth() {
		return shutUp;
	}
}