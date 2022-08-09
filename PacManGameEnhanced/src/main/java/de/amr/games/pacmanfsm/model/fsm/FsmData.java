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

import java.util.Objects;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

/**
 * A finite-state machine together with its textual Graphviz representation. As this text includes information depending
 * on the current state of the machine, it has to be updated at certain intervals.
 * 
 * @author Armin Reichert
 */
public class FsmData implements Comparable<FsmData> {

	private final StateMachine<?, ?> fsm;
	private String graphVizText;

	public FsmData(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		updateGraphVizText();
	}

	@Override
	public int compareTo(FsmData other) {
		return fsm.getDescription().compareTo(other.fsm.getDescription());
	}

	@Override
	public int hashCode() {
		return Objects.hash(graphVizText);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FsmData other = (FsmData) obj;
		return Objects.equals(graphVizText, other.graphVizText);
	}

	public void updateGraphVizText() {
		graphVizText = DotPrinter.toDotFormat(fsm);
	}

	@SuppressWarnings("unchecked")
	public StateMachine<Object, Object> getFsm() {
		return (StateMachine<Object, Object>) fsm;
	}

	public String getGraphVizText() {
		return graphVizText;
	}
}