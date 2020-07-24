package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

class FsmData {

	StateMachine<?, ?> fsm;
	String graphData;
	double scalingEmbedded;
	double scalingWindow;

	public FsmData(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		graphData = DotPrinter.printToString(fsm);
		scalingEmbedded = 1.0;
		scalingWindow = 2.0;
	}

	@Override
	public String toString() {
		return fsm.getDescription();
	}

	public void updateGraph() {
		graphData = DotPrinter.printToString(fsm);
	}
}