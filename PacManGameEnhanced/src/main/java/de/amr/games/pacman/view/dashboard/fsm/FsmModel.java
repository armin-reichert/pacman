package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.statemachine.core.StateMachine;

/**
 * Maintains a list of pairs (state machine, Graphviz representation) that is kept in sync with the
 * currently registered state machines.
 * 
 * @author Armin Reichert
 */
public class FsmModel {

	private List<FsmData> dataList = new ArrayList<>();
	private boolean setChanged;

	public FsmModel() {
		update();
	}

	public void update() {
		Set<StateMachine<?, ?>> machines = dataList.stream().map(FsmData::getFsm).collect(Collectors.toSet());
		if (!REGISTRY.machines().equals(machines)) {
			dataList = REGISTRY.machines().stream().sorted(comparing(StateMachine::getDescription)).map(FsmData::new)
					.collect(toList());
			setChanged = true;
		} else {
			for (FsmData data : dataList) {
				data.updateGraph();
			}
			setChanged = false;
		}
	}

	public boolean setOfMachinesChanged() {
		return setChanged;
	}

	public Stream<FsmData> data() {
		return dataList.stream();
	}
}