package de.amr.games.pacman.view.dashboard.fsm;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.Application.loginfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.StateMachineRegistry;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class FsmView extends JPanel implements Lifecycle {

	static final String GRAPHVIZ_ONLINE = "https://dreampuf.github.io/GraphvizOnline";
	static final String HINT_TEXT = "This area shows the Graphviz representation of the selected finite-state machine";
	static final double MIN_SCALE = 0.2;
	static final double MAX_SCALE = 3.0;

	static class StateMachineInfo {

		StateMachine<?, ?> fsm;
		String dotText;
		double scaling;

		public StateMachineInfo(StateMachine<?, ?> fsm) {
			this.fsm = fsm;
			dotText = DotPrinter.dotText(fsm);
			scaling = 1.0;
		}

		@Override
		public String toString() {
			return fsm.getDescription();
		}
	}

	private Action actionPreviewOnline = new AbstractAction("Preview Online") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedInfo().ifPresent(info -> {
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
			getSelectedInfo().ifPresent(info -> {
				saveDotFile(info.fsm.getDescription() + ".dot", info.dotText);
			});
		}
	};

	private Action actionZoomIn = new AbstractAction("Zoom In") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedInfo().ifPresent(info -> {
				info.scaling += 0.2;
				info.scaling = Math.min(MAX_SCALE, info.scaling);
				updatePreview();
			});
		};
	};

	private Action actionZoomOut = new AbstractAction("Zoom Out") {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSelectedInfo().ifPresent(info -> {
				info.scaling -= 0.2;
				info.scaling = Math.max(MIN_SCALE, info.scaling);
				updatePreview();
			});
		};
	};

	private List<StateMachine<?, ?>> machines;
	private JTree fsmTree;
	private JTextArea textAreaDotPreview;
	private JToolBar toolBar;
	private JButton btnPreview;
	private JButton btnSave;
	private JTabbedPane tabbedPane;
	private JPanel panelSource;
	private JPanel panelPreview;
	private JScrollPane scrollPaneSource;
	private JScrollPane scrollPanePreview;
	private JLabel labelPreview;
	private JButton btnZoomIn;
	private JButton btnZoomOut;

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

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		splitPane.setRightComponent(tabbedPane);

		panelSource = new JPanel();
		tabbedPane.addTab("Source", null, panelSource, null);
		panelSource.setLayout(new BorderLayout(0, 0));

		scrollPaneSource = new JScrollPane();
		panelSource.add(scrollPaneSource);

		textAreaDotPreview = new JTextArea();
		scrollPaneSource.setViewportView(textAreaDotPreview);
		textAreaDotPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textAreaDotPreview.setEditable(false);
		textAreaDotPreview.setTabSize(4);
		textAreaDotPreview.setText(HINT_TEXT);

		panelPreview = new JPanel();
		panelPreview.setBackground(Color.WHITE);
		tabbedPane.addTab("Preview", null, panelPreview, null);
		panelPreview.setLayout(new BorderLayout(0, 0));

		scrollPanePreview = new JScrollPane();
		scrollPanePreview.setBackground(Color.WHITE);
		panelPreview.add(scrollPanePreview, BorderLayout.CENTER);

		labelPreview = new JLabel("");
		labelPreview.setOpaque(true);
		labelPreview.setBackground(Color.WHITE);
		labelPreview.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPanePreview.setViewportView(labelPreview);

		tabbedPane.addChangeListener(change -> {
			if (tabbedPane.getSelectedComponent() == panelPreview) {
				updatePreview();
			}
		});

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		btnSave = new JButton("New button");
		btnSave.setAction(actionSave);
		toolBar.add(btnSave);

		btnPreview = new JButton("Preview");
		toolBar.add(btnPreview);
		btnPreview.setAction(actionPreviewOnline);

		btnZoomIn = new JButton("New button");
		btnZoomIn.setAction(actionZoomIn);
		toolBar.add(btnZoomIn);

		btnZoomOut = new JButton("");
		btnZoomOut.setAction(actionZoomOut);
		toolBar.add(btnZoomOut);

		actionPreviewOnline.setEnabled(false);
		actionSave.setEnabled(false);
		fsmTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fsmTree.addTreeSelectionListener(e -> {
			getSelectedInfo().ifPresentOrElse(info -> {
				textAreaDotPreview.setText(info.dotText);
				if (tabbedPane.getSelectedComponent() == panelPreview) {
					updatePreview();
				}
				actionPreviewOnline.setEnabled(true);
				actionSave.setEnabled(true);
			}, () -> {
				textAreaDotPreview.setText(HINT_TEXT);
				actionPreviewOnline.setEnabled(false);
				actionSave.setEnabled(false);
			});
		});
	}

	private void updatePreview() {
		textAreaDotPreview.setText(HINT_TEXT);
		labelPreview.setIcon(null);
		getSelectedInfo().ifPresent(info -> {
			info.dotText = DotPrinter.dotText(info.fsm);
			textAreaDotPreview.setText(info.dotText);
			textAreaDotPreview.setCaretPosition(0);
			BufferedImage rendered = Graphviz.fromString(info.dotText).scale(info.scaling).render(Format.PNG).toImage();
			labelPreview.setIcon(new ImageIcon(rendered));
		});
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
		updatePreview();
	}

	private void buildTree() {
		machines = new ArrayList<>(StateMachineRegistry.IT.machines());
		machines.sort(Comparator.comparing(StateMachine::getDescription));
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) fsmTree.getModel().getRoot();
		root.removeAllChildren();
		for (StateMachine<?, ?> fsm : machines) {
			root.add(new DefaultMutableTreeNode(new StateMachineInfo(fsm)));
		}
		DefaultTreeModel treeModel = (DefaultTreeModel) fsmTree.getModel();
		treeModel.nodeStructureChanged(root);
		fsmTree.revalidate();
	}

	private Optional<StateMachineInfo> getSelectedInfo() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fsmTree.getLastSelectedPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() instanceof StateMachineInfo) {
			return Optional.of((StateMachineInfo) selectedNode.getUserObject());
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