package de.amr.games.pacman.view.dashboard.fsm;

import java.util.Optional;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.amr.statemachine.core.StateMachine;

public class FsmTree extends DefaultTreeModel {

	private TreePath selectedPath;

	public FsmTree() {
		super(new FsmTreeNode("State Machines"));
		selectedPath = new TreePath(getRoot());
	}

	public void setSelectedPath(TreePath selectedPath) {
		this.selectedPath = selectedPath;
	}

	public TreePath getSelectedPath() {
		return selectedPath;
	}

	public Optional<FsmData> getSelectedData() {
		if (selectedPath != null && selectedPath.getLastPathComponent() != null) {
			FsmTreeNode selectedNode = (FsmTreeNode) selectedPath.getLastPathComponent();
			return Optional.ofNullable(selectedNode).map(FsmTreeNode::getData);
		}
		return Optional.empty();
	}

	public Optional<FsmData> getData(StateMachine<?, ?> fsm) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			FsmTreeNode child = (FsmTreeNode) root.getChildAt(i);
			if (child.getData().getFsm() == fsm) {
				return Optional.of(child.getData());
			}
		}
		return Optional.empty();
	}

	public void rebuild(FsmModel model) {
		FsmTreeNode root = (FsmTreeNode) getRoot();
		root.removeAllChildren();
		model.data().forEach(data -> {
			root.add(new FsmTreeNode(data));
		});
		if (root.getChildCount() > 0) {
			selectedPath = new TreePath(new Object[] { root, root.getChildAt(0) });
		} else {
			selectedPath = new TreePath(root);
		}
		nodeStructureChanged(root);
	}
}