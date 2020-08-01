package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmData {

	public StateMachine<?, ?> fsm;
	public String graph;
	public double scalingEmbedded;
	public double scalingWindow;

	public FsmData(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		graph = DotPrinter.printToString(fsm);
		scalingEmbedded = 1.0;
		scalingWindow = 1.2;
	}

	@Override
	public String toString() {
		return fsm.getDescription();
	}

	public void updateGraph() {
		graph = DotPrinter.printToString(fsm);
	}
}