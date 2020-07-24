package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.controller.StateMachineRegistry;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmView extends JPanel implements Lifecycle {

	static final String GRAPHVIZ_ONLINE_URL = "https://dreampuf.github.io/GraphvizOnline";

	private Action actionViewOnline = new AbstractAction("View Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedInfo().ifPresent(info -> {
				try {
					URI uri = new URI(null, GRAPHVIZ_ONLINE_URL, info.dotText);
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
			getSelectedInfo().ifPresent(info -> {
				saveDotFile(info.fsm.getDescription() + ".dot", info.dotText);
			});
		}
	};

	private Action actionZoomIn = new AbstractAction("Zoom In") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmGraphView.zoomIn();
		};
	};

	private Action actionZoomOut = new AbstractAction("Zoom Out") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmGraphView.zoomOut();
		};
	};

	private List<StateMachine<?, ?>> machines;

	private TreePath selectedPath;
	private JTree tree;
	private JToolBar toolBar;
	private JButton btnPreview;
	private JButton btnSave;
	private JTabbedPane tabbedPane;
	private FsmTextView fsmTextView;
	private FsmGraphView fsmGraphView;
	private JButton btnZoomIn;
	private JButton btnZoomOut;

	public FsmView() {
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);

		JScrollPane fsmTreeScrollPane = new JScrollPane();
		fsmTreeScrollPane.setMinimumSize(new Dimension(200, 25));
		splitPane.setLeftComponent(fsmTreeScrollPane);

		tree = new JTree();
		fsmTreeScrollPane.setViewportView(tree);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("State Machines");
		tree.setModel(new DefaultTreeModel(root));

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		splitPane.setRightComponent(tabbedPane);

		fsmTextView = new FsmTextView();
		tabbedPane.addTab("Source", null, fsmTextView, null);

		fsmGraphView = new FsmGraphView();
		tabbedPane.addTab("Preview", null, fsmGraphView, null);

		tabbedPane.addChangeListener(change -> {
			if (tabbedPane.getSelectedComponent() == fsmGraphView) {
				update();
			}
		});

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		btnSave = new JButton("New button");
		btnSave.setAction(actionSave);
		toolBar.add(btnSave);

		btnPreview = new JButton("Preview");
		toolBar.add(btnPreview);
		btnPreview.setAction(actionViewOnline);

		btnZoomIn = new JButton("New button");
		btnZoomIn.setAction(actionZoomIn);
		toolBar.add(btnZoomIn);

		btnZoomOut = new JButton("");
		btnZoomOut.setAction(actionZoomOut);
		toolBar.add(btnZoomOut);

		actionViewOnline.setEnabled(false);
		actionSave.setEnabled(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this::onTreeSelectionChange);
	}

	private void onTreeSelectionChange(TreeSelectionEvent e) {
		selectedPath = e.getNewLeadSelectionPath();
		FsmViewNodeInfo info = getSelectedInfo().orElse(null);
		if (info != null) {
			info.dotText = DotPrinter.dotText(info.fsm);
		}
		if (tabbedPane.getSelectedComponent() == fsmGraphView) {
			fsmGraphView.setFsmInfo(info);
		} else {
			fsmTextView.setFsmInfo(info);
		}
		Stream.of(actionViewOnline, actionSave, actionZoomIn, actionZoomOut)
				.forEach(action -> action.setEnabled(info != null));
	}

	@Override
	public void init() {
		// not used
	}

	@Override
	public void update() {
		if (machines == null || !new HashSet<>(machines).equals(StateMachineRegistry.IT.machines())) {
			buildTree();
		}
		FsmViewNodeInfo info = getSelectedInfo().orElse(null);
		if (info != null) {
			info.dotText = DotPrinter.dotText(info.fsm);
		}
		fsmGraphView.setFsmInfo(info);
		fsmTextView.setFsmInfo(info);
	}

	private void buildTree() {
		machines = new ArrayList<>(StateMachineRegistry.IT.machines());
		machines.sort(Comparator.comparing(StateMachine::getDescription));
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		root.removeAllChildren();
		for (StateMachine<?, ?> fsm : machines) {
			root.add(new DefaultMutableTreeNode(new FsmViewNodeInfo(fsm)));
		}
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.nodeStructureChanged(root);
		if (root.getChildCount() > 0) {
			selectedPath = new TreePath(new Object[] { root, root.getChildAt(0) });
		} else {
			selectedPath = new TreePath(root);
		}
		tree.setSelectionPath(selectedPath);
	}

	private Optional<FsmViewNodeInfo> getSelectedInfo() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() instanceof FsmViewNodeInfo) {
			return Optional.of((FsmViewNodeInfo) selectedNode.getUserObject());
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