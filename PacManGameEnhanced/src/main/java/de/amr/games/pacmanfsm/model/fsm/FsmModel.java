/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.model.fsm;

import static de.amr.easy.game.controller.StateMachineRegistry.REGISTRY;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Maintains a map of data (state machine, Graphviz representation) that is kept in sync with the currently registered
 * set of state machines.
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
			REGISTRY.categories()
					.forEach(category -> dataByCategory.put(category, REGISTRY.machines(category).map(FsmData::new).toList()));
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