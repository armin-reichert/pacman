package de.amr.games.pacman.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.statemachine.core.StateMachine;

/**
 * A central place to register/unregister the used state machines.
 * 
 * @author Armin Reichert
 */
public final class StateMachineRegistry {

	private StateMachineRegistry() {
		FSM_LOGGER.setLevel(Level.OFF);
	}

	private static final Logger FSM_LOGGER = Logger.getLogger(StateMachineRegistry.class.getName());

	public static final StateMachineRegistry IT = new StateMachineRegistry();

	private final Set<StateMachine<?, ?>> machines = new HashSet<>();

	public Collection<StateMachine<?, ?>> machines() {
		return Collections.unmodifiableSet(machines);
	}

	public void loginfo(String message, Object... args) {
		FSM_LOGGER.info(String.format(message, args));
	}

	public void toggleLogging() {
		FSM_LOGGER.setLevel(isLoggingEnabled() ? Level.OFF : Level.INFO);
	}

	public void setLogging(boolean enabled) {
		FSM_LOGGER.setLevel(enabled ? Level.INFO : Level.OFF);
	}

	public boolean isLoggingEnabled() {
		return FSM_LOGGER.getLevel() == Level.INFO;
	}

	public <FSM extends StateMachine<?, ?>> void register(Object registrar, Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			fsm.getTracer().setLogger(FSM_LOGGER);
			this.machines.add(fsm);
			Application.loginfo("State machine registered by %s: %s", registrar, fsm);
		});
	}

	public <FSM extends StateMachine<?, ?>> void unregister(Object registrar, Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			if (fsm != null) {
				fsm.getTracer().setLogger(Logger.getGlobal());
				this.machines.remove(fsm);
				Application.loginfo("State machine unregistered by %s: %s", registrar, fsm);
			}
		});
	}
}