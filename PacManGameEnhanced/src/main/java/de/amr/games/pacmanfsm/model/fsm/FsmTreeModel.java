/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.model.fsm;

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