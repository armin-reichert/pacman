package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maintains a map of data (state machine, Graphviz representation) that is kept in sync with the
 * currently registered set of state machines.
 * 
 * @author Armin Reichert
 */
public class FsmModel {

	private final Map<String, List<FsmData>> dataByCategory = new ConcurrentHashMap<>();
	private boolean setOfMachinesChanged;

	public void update() {
		setOfMachinesChanged = false;
		Set<?> modelMachineSet = data().map(FsmData::getFsm).collect(Collectors.toSet());
		Set<?> registeredMachineSet = REGISTRY.machines().collect(Collectors.toSet());
		if (!registeredMachineSet.equals(modelMachineSet)) {
			dataByCategory.clear();
			REGISTRY.categories().forEach(category -> {
				dataByCategory.put(category, REGISTRY.machines(category).map(FsmData::new).collect(Collectors.toList()));
			});
			setOfMachinesChanged = true;
		} else {
			data().forEach(FsmData::updateGraph);
		}
	}

	public Stream<String> categories() {
		return dataByCategory.keySet().stream();
	}

	public Stream<FsmData> data(String category) {
		return dataByCategory.get(category).stream();
	}

	public Stream<FsmData> data() {
		return dataByCategory.values().stream().flatMap(List::stream);
	}

	public boolean setOfMachinesChanged() {
		return setOfMachinesChanged;
	}
}