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

	public static final StateMachineRegistry IT = new StateMachineRegistry();

	private final Logger logger = Logger.getLogger(getClass().getName());
	private final Set<StateMachine<?, ?>> machines = new HashSet<>();

	private StateMachineRegistry() {
		logger.setLevel(Level.OFF);
	}

	public Collection<StateMachine<?, ?>> machines() {
		return Collections.unmodifiableSet(machines);
	}

	public void loginfo(String message, Object... args) {
		logger.info(String.format(message, args));
	}

	public void toggleLogging() {
		logger.setLevel(isLoggingEnabled() ? Level.OFF : Level.INFO);
	}

	public void setLogging(boolean enabled) {
		logger.setLevel(enabled ? Level.INFO : Level.OFF);
	}

	public boolean isLoggingEnabled() {
		return logger.getLevel() == Level.INFO;
	}

	public <FSM extends StateMachine<?, ?>> void register(Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			fsm.getTracer().setLogger(logger);
			this.machines.add(fsm);
			Application.loginfo("State machine registered: %s", fsm);
		});
	}

	public <FSM extends StateMachine<?, ?>> void unregister(Stream<FSM> machines) {
		machines.filter(Objects::nonNull).forEach(fsm -> {
			if (fsm != null) {
				fsm.getTracer().setLogger(Logger.getGlobal());
				this.machines.remove(fsm);
				Application.loginfo("State machine unregistered: %s", fsm);
			}
		});
	}
}