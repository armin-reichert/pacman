package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.statemachine.core.StateMachine;

public class FsmModel {

	private List<FsmData> machines = new ArrayList<>();
	private boolean valid;

	public FsmModel() {
		rebuild();
	}

	public Stream<FsmData> data() {
		return machines.stream();
	}

	public void checkIfValid() {
		valid = REGISTRY.machines().equals(machines.stream().map(FsmData::getFsm).collect(Collectors.toSet()));
	}

	public boolean isValid() {
		return valid;
	}

	public void rebuild() {
		machines = REGISTRY.machines().stream().sorted(comparing(StateMachine::getDescription)).map(FsmData::new)
				.collect(toList());
		valid = true;
	}
}