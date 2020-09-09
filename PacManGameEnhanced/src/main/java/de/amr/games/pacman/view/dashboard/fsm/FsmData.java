package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

/**
 * A finite-state machine together with its textual Graphviz representation. As this text includes
 * information depending on the current state of the machine, it has to be updated at certain
 * intervals.
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

	public void updateGraphVizText() {
		graphVizText = DotPrinter.toDotFormat(fsm);
	}

	public StateMachine<?, ?> getFsm() {
		return fsm;
	}

	public String getGraphVizText() {
		return graphVizText;
	}
}