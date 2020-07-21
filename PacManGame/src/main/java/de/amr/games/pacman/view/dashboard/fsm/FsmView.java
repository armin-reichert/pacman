package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

	private Action actionExternalPreview = new AbstractAction("Preview Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			NodeInfo info = getSelectedNodeInfo();
			if (info != null) {
				try {
					String hashValue = URLEncoder.encode(info.dotText, StandardCharsets.UTF_8.toString());
					hashValue = hashValue.replace('+', ' ');
					openURL("https://dreampuf.github.io/GraphvizOnline/#" + hashValue);
				} catch (UnsupportedEncodingException x) {
					x.printStackTrace();
				}
			}
		}
	};

	private Action actionSave = new AbstractAction("Save") {
		@Override
		public void actionPerformed(ActionEvent e) {
			NodeInfo info = getSelectedNodeInfo();
			if (info != null) {
				saveDotFile(info.fsm.getDescription() + ".dot", info.dotText);
			}
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
		btnPreview.setAction(actionExternalPreview);

		actionExternalPreview.setEnabled(false);
		actionSave.setEnabled(false);
		fsmTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fsmTree.addTreeSelectionListener(e -> {
			NodeInfo info = getSelectedNodeInfo();
			if (info != null) {
				dotPreview.setText(info.dotText);
				actionExternalPreview.setEnabled(true);
				actionSave.setEnabled(true);
			} else {
				dotPreview.setText(HINT_TEXT);
				actionExternalPreview.setEnabled(false);
				actionSave.setEnabled(false);
			}
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
		fsmTree.revalidate();
	}

	private NodeInfo getSelectedNodeInfo() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fsmTree.getLastSelectedPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() instanceof NodeInfo) {
			return (NodeInfo) selectedNode.getUserObject();
		}
		return null;
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

	// TODO only works under Windows
	private void openURL(String url) {
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}