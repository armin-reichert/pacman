package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

class FsmData {

	StateMachine<?, ?> fsm;
	String graph;
	double scalingEmbedded;
	double scalingWindow;

	public FsmData(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		graph = DotPrinter.printToString(fsm);
		scalingEmbedded = 1.2;
		scalingWindow = 1.6;
	}

	@Override
	public String toString() {
		return fsm.getDescription();
	}

	public void updateGraph() {
		graph = DotPrinter.printToString(fsm);
	}
}