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
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
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
				fsmWindowGraphView = new FsmGraphView();
				fsmWindow.getContentPane().add(fsmWindowGraphView);
				fsmWindowGraphView.getInputMap().put(KeyStroke.getKeyStroke('+'), actionWindowZoomIn);
				fsmWindowGraphView.getActionMap().put(actionWindowZoomIn, actionWindowZoomIn);
				fsmWindowGraphView.getInputMap().put(KeyStroke.getKeyStroke('-'), actionWindowZoomOut);
				fsmWindowGraphView.getActionMap().put(actionWindowZoomOut, actionWindowZoomOut);
			}
			fsmWindowGraphView.requestFocus();
			fsmWindow.setVisible(true);
		}
	};

	private Action actionViewOnline = new AbstractAction("View Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			treeModel.getSelectedNodeInfo().ifPresent(info -> {
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
			treeModel.getSelectedNodeInfo().ifPresent(info -> {
				saveDotFile(info.fsm.getDescription() + ".dot", info.dotText);
			});
		}
	};

	private Action actionEmbeddedZoomIn = new AbstractAction("Zoom In") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmEmbeddedGraphView.zoomIn();
		};
	};

	private Action actionEmbeddedZoomOut = new AbstractAction("Zoom Out") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmEmbeddedGraphView.zoomOut();
		};
	};

	private Action actionWindowZoomIn = new AbstractAction("Zoom In") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmWindowGraphView.zoomIn();
		};
	};

	private Action actionWindowZoomOut = new AbstractAction("Zoom Out") {
		@Override
		public void actionPerformed(ActionEvent e) {
			fsmWindowGraphView.zoomOut();
		};
	};

	private List<StateMachine<?, ?>> machines;
	private JFrame fsmWindow;
	private FsmGraphView fsmWindowGraphView;
	private FsmViewTree treeModel;
	private JTree tree;
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

		tree = new JTree();
		fsmTreeScrollPane.setViewportView(tree);
		treeModel = new FsmViewTree();
		tree.setModel(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this::onTreeSelectionChange);

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		splitPane.setRightComponent(tabbedPane);

		fsmEmbeddedTextView = new FsmTextView();
		tabbedPane.addTab("Source", null, fsmEmbeddedTextView, null);

		fsmEmbeddedGraphView = new FsmGraphView();
		tabbedPane.addTab("Preview", null, fsmEmbeddedGraphView, null);

		tabbedPane.addChangeListener(change -> {
			if (tabbedPane.getSelectedComponent() == fsmEmbeddedGraphView) {
				update();
			}
		});

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
		btnZoomIn.setAction(actionEmbeddedZoomIn);
		toolBar.add(btnZoomIn);

		btnZoomOut = new JButton("");
		btnZoomOut.setAction(actionEmbeddedZoomOut);
		toolBar.add(btnZoomOut);

	}

	private void onTreeSelectionChange(TreeSelectionEvent e) {
		treeModel.setSelectedPath(e.getNewLeadSelectionPath());
		FsmViewTreeNode info = treeModel.getSelectedNodeInfo().orElse(null);
		if (info != null) {
			info.dotText = DotPrinter.dotText(info.fsm);
			fsmEmbeddedGraphView.setScaling(info.scalingEmbedded);
			if (fsmWindowGraphView != null) {
				fsmWindowGraphView.setScaling(info.scalingWindow);
			}
		}
	}

	@Override
	public void init() {
		// not used
	}

	@Override
	public void update() {

		// model changed?
		if (machines == null || !new HashSet<>(machines).equals(StateMachineRegistry.IT.machines())) {
			machines = new ArrayList<>(StateMachineRegistry.IT.machines());
			machines.sort(Comparator.comparing(StateMachine::getDescription));
			treeModel.rebuild(machines);
			tree.setSelectionPath(treeModel.getSelectedPath());
		}

		// update for current selection
		FsmViewTreeNode selectedInfo = treeModel.getSelectedNodeInfo().orElse(null);
		if (selectedInfo != null) {
			selectedInfo.dotText = DotPrinter.dotText(selectedInfo.fsm);
		}

		Stream.of(actionViewOnline, actionSave, actionEmbeddedZoomIn, actionEmbeddedZoomOut, actionWindowZoomIn,
				actionWindowZoomOut).forEach(action -> action.setEnabled(selectedInfo != null));

		fsmEmbeddedGraphView.setFsmInfo(selectedInfo);
		fsmEmbeddedTextView.setFsmInfo(selectedInfo);

		if (fsmWindow != null && fsmWindow.isVisible()) {
			fsmWindowGraphView.setFsmInfo(selectedInfo);
			if (selectedInfo != null) {
				fsmWindow.setTitle("State Machine View: " + selectedInfo.fsm.getDescription());
			} else {
				fsmWindow.setTitle("State Machine View: No machine selected");
			}
		}
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