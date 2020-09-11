package de.amr.games.pacman.model.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
		Set<?> myMachineSet = data().map(FsmData::getFsm).collect(toSet());
		Set<?> registeredMachineSet = REGISTRY.machines().collect(toSet());
		setOfMachinesChanged = !registeredMachineSet.equals(myMachineSet);
		if (setOfMachinesChanged) {
			dataByCategory.clear();
			REGISTRY.categories().forEach(category -> {
				dataByCategory.put(category, REGISTRY.machines(category).map(FsmData::new).collect(toList()));
			});
		} else {
			data().forEach(FsmData::updateGraphVizText);
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