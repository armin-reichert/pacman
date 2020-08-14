package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmData {

	private final StateMachine<?, ?> fsm;
	private String graph;

	public FsmData(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		updateGraph();
	}

	public void updateGraph() {
		graph = DotPrinter.toDotFormat(fsm);
	}

	public StateMachine<?, ?> getFsm() {
		return fsm;
	}

	public String getGraph() {
		return graph;
	}
}