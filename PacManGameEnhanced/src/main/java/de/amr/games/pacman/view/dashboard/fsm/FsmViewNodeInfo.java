package de.amr.games.pacman.view.dashboard.fsm;

import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

class FsmViewNodeInfo {

	StateMachine<?, ?> fsm;
	String dotText;
	double scaling;

	public FsmViewNodeInfo(StateMachine<?, ?> fsm) {
		this.fsm = fsm;
		dotText = DotPrinter.dotText(fsm);
		scaling = 1.6;
	}

	@Override
	public String toString() {
		return fsm.getDescription();
	}
}