package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.PacManApp;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmView extends JPanel implements Lifecycle {

	static final String GRAPHVIZ_ONLINE = "https://dreampuf.github.io/GraphvizOnline";
	static final String HINT_TEXT = "This area shows the Graphviz representation of the selected finite-state machine";

	static class NodeInfo {

		final StateMachine<?, ?> fsm;
		final String dotText;

		public NodeInfo(StateMachine<?, ?> fsm) {
			this.fsm = fsm;
			dotText = DotPrinter.dotText(fsm);
		}

		@Override
		public String toString() {
			return fsm.getDescription();
		}
	}

	private Action actionPreviewOnline = new AbstractAction("Preview Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedNodeInfo().ifPresent(info -> {
				try {
					URI uri = new URI(null, GRAPHVIZ_ONLINE, info.dotText);
					Desktop.getDesktop().browse(uri);
				} catch (Exception x) {
					x.printStackTrace();
				}
			});
		}
	};

	private Action actionSave = new AbstractAction("Save") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedNodeInfo().ifPresent(info -> {
				saveDotFile(info.fsm.getDescription() + ".dot", info.dotText);
			});
		}
	};

	private List<StateMachine<?, ?>> machines;
	private JTree fsmTree;
	private JScrollPane dotPreviewScrollPane;
	private JTextArea dotPreview;
	private JToolBar toolBar;
	private JButton btnPreview;
	private JButton btnSave;

	public FsmView() {
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.33);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		JScrollPane fsmTreeScrollPane = new JScrollPane();
		splitPane.setLeftComponent(fsmTreeScrollPane);

		fsmTree = new JTree();
		fsmTreeScrollPane.setViewportView(fsmTree);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("State Machines");
		fsmTree.setModel(new DefaultTreeModel(root));

		dotPreviewScrollPane = new JScrollPane();
		splitPane.setRightComponent(dotPreviewScrollPane);

		dotPreview = new JTextArea();
		dotPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
		dotPreview.setEditable(false);
		dotPreview.setTabSize(4);
		dotPreview.setText(HINT_TEXT);
		dotPreviewScrollPane.setViewportView(dotPreview);

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		btnSave = new JButton("New button");
		btnSave.setAction(actionSave);
		toolBar.add(btnSave);

		btnPreview = new JButton("Preview");
		toolBar.add(btnPreview);
		btnPreview.setAction(actionPreviewOnline);

		actionPreviewOnline.setEnabled(false);
		actionSave.setEnabled(false);
		fsmTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fsmTree.addTreeSelectionListener(e -> {
			getSelectedNodeInfo().ifPresentOrElse(info -> {
				dotPreview.setText(info.dotText);
				actionPreviewOnline.setEnabled(true);
				actionSave.setEnabled(true);
			}, () -> {
				dotPreview.setText(HINT_TEXT);
				actionPreviewOnline.setEnabled(false);
				actionSave.setEnabled(false);
			});
		});
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (machines == null || machines.size() != PacManApp.REGISTERED_FSMs.size()) {
			buildTree();
		}
	}

	private void buildTree() {
		machines = new ArrayList<>(PacManApp.REGISTERED_FSMs.values());
		machines.sort(Comparator.comparing(StateMachine::getDescription));
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) fsmTree.getModel().getRoot();
		root.removeAllChildren();
		for (StateMachine<?, ?> fsm : machines) {
			root.add(new DefaultMutableTreeNode(new NodeInfo(fsm)));
		}
		DefaultTreeModel treeModel = (DefaultTreeModel) fsmTree.getModel();
		treeModel.nodeStructureChanged(root);
		fsmTree.revalidate();
	}

	private Optional<NodeInfo> getSelectedNodeInfo() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fsmTree.getLastSelectedPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() instanceof NodeInfo) {
			return Optional.of((NodeInfo) selectedNode.getUserObject());
		}
		return Optional.empty();
	}

	private void saveDotFile(String fileName, String dotText) {
		JFileChooser saveDialog = new JFileChooser();
		saveDialog.setDialogTitle("Save state machine as DOT file");
		saveDialog.setSelectedFile(new File(fileName));
		int option = saveDialog.showSaveDialog(app().shell().get().f2Dialog);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = saveDialog.getSelectedFile();
			try (FileWriter w = new FileWriter(saveDialog.getSelectedFile())) {
				w.write(dotText);
			} catch (Exception x) {
				loginfo("DOT file could not be written", file);
			}
		}
	}
}