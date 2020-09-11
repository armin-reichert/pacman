package de.amr.games.pacman.model.fsm;

import java.util.Optional;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.amr.statemachine.core.StateMachine;

public class FsmTreeModel extends DefaultTreeModel {

	private TreePath selectedPath;

	public FsmTreeModel() {
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
			if (selectedNode != null) {
				FsmData data = (FsmData) selectedNode.getUserObject();
				return Optional.ofNullable(data);
			}
		}
		return Optional.empty();
	}

	public Optional<FsmData> getData(StateMachine<?, ?> fsm) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			FsmTreeNode categoryNode = (FsmTreeNode) root.getChildAt(i);
			for (int j = 0; j < categoryNode.getChildCount(); ++j) {
				FsmTreeNode child = (FsmTreeNode) root.getChildAt(j);
				FsmData data = (FsmData) child.getUserObject();
				if (data.getFsm() == fsm) {
					return Optional.of(data);
				}
			}
		}
		return Optional.empty();
	}

	public void rebuild(FsmModel model) {
		FsmTreeNode root = (FsmTreeNode) getRoot();
		root.removeAllChildren();
		model.categories().sorted().forEach(category -> {
			FsmTreeNode categoryNode = new FsmTreeNode(category);
			root.add(categoryNode);
			model.data(category).sorted().forEach(data -> {
				FsmTreeNode fsmNode = new FsmTreeNode(data.getFsm().getDescription());
				fsmNode.setUserObject(data);
				categoryNode.add(fsmNode);
			});
		});
		nodeStructureChanged(root);
	}

	public void initSelection() {
		if (root.getChildCount() > 0 && root.getChildAt(0).getChildCount() > 0) {
			selectedPath = new TreePath(new Object[] { root, root.getChildAt(0), root.getChildAt(0).getChildAt(0) });
		} else {
			selectedPath = new TreePath(root);
		}
	}
}