package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

class FsmViewTreeNode {

	StateMachine<?, ?> fsm;
	String dotText;
	double scalingEmbedded;
	double scalingWindow;

	public FsmViewTreeNode(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		dotText = DotPrinter.dotText(fsm);
		scalingEmbedded = 1.0;
		scalingWindow = 2.0;
	}

	@Override
	public String toString() {
		return fsm.getDescription();
	}
}