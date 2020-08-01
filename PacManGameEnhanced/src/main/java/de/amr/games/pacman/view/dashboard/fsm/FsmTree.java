package de.amr.games.pacman.view.dashboard.fsm;

import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
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

	public Optional<FsmData> getData(StateMachine<?, ?> fsm) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			TreeNode child = root.getChildAt(i);
			if (child instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
				if (node.getUserObject() instanceof FsmData) {
					FsmData data = (FsmData) node.getUserObject();
					if (data.fsm == fsm) {
						return Optional.of(data);
					}
				}
			}
		}
		return Optional.empty();
	}

	public void rebuild(FsmModel model) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
		root.removeAllChildren();
		for (StateMachine<?, ?> fsm : model.machines()) {
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