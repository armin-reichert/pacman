package de.amr.games.pacman.view.dashboard.fsm;

import javax.swing.tree.DefaultMutableTreeNode;

public class FsmTreeNode extends DefaultMutableTreeNode {

	private String label;

	public FsmTreeNode(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		if (label != null) {
			return label;
		}
		if (getUserObject() != null) {
			FsmData data = (FsmData) getUserObject();
			return data.getFsm().getDescription();
		}
		return super.toString();
	}
}