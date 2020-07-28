package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.Application.loginfo;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.controller.StateMachineRegistry;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmView extends JPanel implements Lifecycle {

	static final String GRAPHVIZ_ONLINE_URL = "https://dreampuf.github.io/GraphvizOnline";

	private Action actionViewInWindow = new AbstractAction("View in Window") {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (fsmWindow == null) {
				fsmWindow = new JFrame();
				fsmWindow.setSize(800, 600);
				fsmWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				fsmWindow.setLocationRelativeTo(null);
				fsmWindowGraphView = new FsmGraphView();
				fsmWindow.getContentPane().add(fsmWindowGraphView);
			}
			fsmWindowGraphView.requestFocus();
			fsmWindow.setVisible(true);
		}
	};

	private Action actionViewOnline = new AbstractAction("View Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			tree.getSelectedData().ifPresent(data -> {
				try {
					URI uri = new URI(null, GRAPHVIZ_ONLINE_URL, data.graph);
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
			tree.getSelectedData().ifPresent(data -> {
				saveDotFile(data.fsm.getDescription() + ".dot", data.graph);
			});
		}
	};

	private LinkedHashSet<StateMachine<?, ?>> machines;
	private JFrame fsmWindow;
	private FsmGraphView fsmWindowGraphView;
	private FsmTree tree;
	private JTree treeView;
	private JToolBar toolBar;
	private JButton btnViewOnline;
	private JButton btnSave;
	private JTabbedPane tabbedPane;
	private FsmTextView fsmEmbeddedTextView;
	private FsmGraphView fsmEmbeddedGraphView;
	private JButton btnZoomIn;
	private JButton btnZoomOut;
	private JButton btnViewInWindow;

	public FsmView() {
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);

		JScrollPane fsmTreeScrollPane = new JScrollPane();
		fsmTreeScrollPane.setMinimumSize(new Dimension(200, 25));
		splitPane.setLeftComponent(fsmTreeScrollPane);

		treeView = new JTree();
		fsmTreeScrollPane.setViewportView(treeView);
		tree = new FsmTree();
		treeView.setModel(tree);
		treeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeView.addTreeSelectionListener(this::onTreeViewSelectionChange);
		treeView.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onTreeViewMousePressed(e);
			}
		});

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		splitPane.setRightComponent(tabbedPane);

		fsmEmbeddedTextView = new FsmTextView();

		fsmEmbeddedGraphView = new FsmGraphView();
		fsmEmbeddedGraphView.setEmbedded(true);

		tabbedPane.addTab("Graph", null, fsmEmbeddedGraphView, null);
		tabbedPane.addTab("Source", null, fsmEmbeddedTextView, null);

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		btnSave = new JButton("New button");
		btnSave.setAction(actionSave);
		toolBar.add(btnSave);

		btnViewInWindow = new JButton("New button");
		btnViewInWindow.setAction(actionViewInWindow);
		toolBar.add(btnViewInWindow);

		btnViewOnline = new JButton("Preview");
		toolBar.add(btnViewOnline);
		btnViewOnline.setAction(actionViewOnline);

		btnZoomIn = new JButton("New button");
		btnZoomIn.setAction(fsmEmbeddedGraphView.actionZoomIn);
		toolBar.add(btnZoomIn);

		btnZoomOut = new JButton("");
		btnZoomOut.setAction(fsmEmbeddedGraphView.actionZoomOut);
		toolBar.add(btnZoomOut);
	}

	private void onTreeViewSelectionChange(TreeSelectionEvent e) {
		tree.setSelectedPath(e.getNewLeadSelectionPath());
		FsmData node = tree.getSelectedData().orElse(null);
		if (node != null) {
			node.graph = DotPrinter.printToString(node.fsm);
		}
	}

	private void onTreeViewMousePressed(MouseEvent e) {
		TreePath path = treeView.getPathForLocation(e.getX(), e.getY());
		if (path != null && e.getClickCount() == 2) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node != null && node.getUserObject() instanceof FsmData) {
				actionViewInWindow.actionPerformed(null);
			}
		}
	}

	@Override
	public void init() {
		// not used
	}

	@Override
	public void update() {
		if (machines == null || !machines.equals(StateMachineRegistry.REGISTRY.machines())) {
			List<StateMachine<?, ?>> machinesList = new ArrayList<>(StateMachineRegistry.REGISTRY.machines());
			machinesList.sort(Comparator.comparing(StateMachine::getDescription));
			machines = new LinkedHashSet<>(machinesList);
			tree.rebuild(machinesList);
			treeView.setSelectionPath(tree.getSelectedPath());
		}
		FsmData data = tree.getSelectedData().orElse(null);
		if (data != null) {
			data.updateGraph();
		}
		fsmEmbeddedGraphView.setData(data);
		fsmEmbeddedTextView.setData(data);
		if (fsmWindow != null) {
			fsmWindow.setTitle("State Machine: " + (data != null ? data.fsm.getDescription() : "No machine selected"));
			fsmWindowGraphView.setData(data);
		}
		actions().forEach(action -> action.setEnabled(data != null));
	}

	private Stream<Action> actions() {
		Stream<Action> actions = Stream.of(actionViewOnline, actionSave, fsmEmbeddedGraphView.actionZoomIn,
				fsmEmbeddedGraphView.actionZoomOut);
		return fsmWindow != null
				? Stream.concat(Stream.of(fsmWindowGraphView.actionZoomIn, fsmWindowGraphView.actionZoomOut), actions)
				: actions;
	}

	private void saveDotFile(String fileName, String dotText) {
		JFileChooser saveDialog = new JFileChooser();
		saveDialog.setDialogTitle("Save state machine as DOT file");
		saveDialog.setSelectedFile(new File(fileName));
		int option = saveDialog.showSaveDialog(this);
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