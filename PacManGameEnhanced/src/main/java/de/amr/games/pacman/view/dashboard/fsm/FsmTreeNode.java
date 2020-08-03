package de.amr.games.pacman.view.dashboard.fsm;

import javax.swing.tree.DefaultMutableTreeNode;

public class FsmTreeNode extends DefaultMutableTreeNode {

	private String label;

	public FsmTreeNode(String label) {
		this.label = label;
	}

	public FsmTreeNode(FsmData data) {
		setUserObject(data);
	}

	public FsmData getData() {
		return (FsmData) getUserObject();
	}

	@Override
	public String toString() {
		if (label != null) {
			return label;
		}
		return getData() != null ? getData().getFsm().getDescription() : "";
	}
}