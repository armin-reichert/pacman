package de.amr.games.pacman.view.dashboard.fsm;

import java.util.List;
import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.amr.statemachine.core.StateMachine;

public class FsmTree extends DefaultTreeModel {

	private TreePath selectedPath;

	public FsmTree() {
		super(new DefaultMutableTreeNode("State Machines"));
		selectedPath = new TreePath(getRoot());
	}

	public void setSelectedPath(TreePath selectedPath) {
		this.selectedPath = selectedPath;
	}

	public TreePath getSelectedPath() {
		return selectedPath;
	}

	public Optional<FsmData> getSelectedData() {
		if (selectedPath == null) {
			return Optional.empty();
		}
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() instanceof FsmData) {
			return Optional.of((FsmData) selectedNode.getUserObject());
		}
		return Optional.empty();
	}

	public void rebuild(List<StateMachine<?, ?>> machines) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
		root.removeAllChildren();
		for (StateMachine<?, ?> fsm : machines) {
			root.add(new DefaultMutableTreeNode(new FsmData(fsm)));
		}
		nodeStructureChanged(root);
		if (root.getChildCount() > 0) {
			selectedPath = new TreePath(new Object[] { root, root.getChildAt(0) });
		} else {
			selectedPath = new TreePath(root);
		}
	}
}