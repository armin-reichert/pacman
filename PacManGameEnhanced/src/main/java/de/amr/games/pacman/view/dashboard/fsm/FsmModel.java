package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public final Map<String, List<FsmData>> data = new HashMap<>();
	private boolean setChanged;

	public FsmModel() {
		update();
	}

	public Stream<FsmData> data() {
		return data.values().stream().flatMap(List::stream);
	}

	public void update() {
		Set<StateMachine<?, ?>> machines = data().map(FsmData::getFsm).collect(Collectors.toSet());
		if (!REGISTRY.machines().equals(machines)) {
			data.clear();
			REGISTRY.categories().forEach(category -> {
				REGISTRY.machines(category).forEach(machine -> {
					data.put(category, new ArrayList<>());
				});
			});
			REGISTRY.categories().forEach(category -> {
				REGISTRY.machines(category).forEach(machine -> {
					data.get(category).add(new FsmData(machine));
				});
			});
			setChanged = true;
		} else {
			data().forEach(FsmData::updateGraph);
			setChanged = false;
		}
	}

	public boolean setOfMachinesChanged() {
		return setChanged;
	}
}