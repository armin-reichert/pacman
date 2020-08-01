package de.amr.games.pacman.view.dashboard.fsm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.controller.StateMachineRegistry;
import de.amr.statemachine.core.StateMachine;

public class FsmModel implements Lifecycle {

	private LinkedHashSet<StateMachine<?, ?>> machines;
	private boolean changed;

	@Override
	public void init() {
	}

	@Override
	public void update() {
		changed = false;
		if (machines == null || !machines.equals(StateMachineRegistry.REGISTRY.machines())) {
			List<StateMachine<?, ?>> machinesList = new ArrayList<>(StateMachineRegistry.REGISTRY.machines());
			machinesList.sort(Comparator.comparing(StateMachine::getDescription));
			machines = new LinkedHashSet<>(machinesList);
			changed = true;
		}
	}

	public Set<StateMachine<?, ?>> machines() {
		return machines;
	}

	public boolean hasChanged() {
		return changed;
	}
}