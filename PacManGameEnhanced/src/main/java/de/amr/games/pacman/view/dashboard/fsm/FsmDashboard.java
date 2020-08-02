package de.amr.games.pacman.view.dashboard.fsm;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.dot.DotPrinter;

public class FsmDashboard implements Lifecycle {

	static int WIDTH = 1024, HEIGHT = 700;

	private class FsmWindow extends JInternalFrame {
		private FsmGraphView graphView;
		private FsmData data;

		public FsmWindow(StateMachine<?, ?> fsm) {
			setResizable(true);
			setClosable(true);
			setResizable(true);
			setIconifiable(true);
			setMaximizable(true);
			try {
				setMaximum(false);
			} catch (PropertyVetoException x) {
				x.printStackTrace();
			}
			data = new FsmData(fsm);
			data.graph = DotPrinter.printToString(fsm);
			setTitle(fsm.getDescription());
			setSize(desktop.getWidth(), 250);
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			setLocation(0, 25 * fsmWindowMap.size());
			graphView = new FsmGraphView();
			graphView.setData(data);
			getContentPane().add(graphView);
		}
	}

	private final FsmModel model;
	public final JFrame window;
	private final JDesktopPane desktop;
	private final Map<StateMachine<?, ?>, FsmWindow> fsmWindowMap = new HashMap<>();

	public FsmDashboard(FsmModel model) {
		this.model = model;
		desktop = new JDesktopPane();
		desktop.setSize(WIDTH, HEIGHT);
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		window = new JFrame("Pac-Man State Machines Dashboard");
		window.setContentPane(desktop);
		window.setSize(WIDTH + 5, HEIGHT + 5);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		model.machines().forEach(fsm -> {
			addStateMachine(fsm);
		});
		for (Map.Entry<?, FsmWindow> entry : fsmWindowMap.entrySet()) {
			if (!model.machines().contains(entry.getKey())) {
				desktop.remove(entry.getValue());
				fsmWindowMap.remove(entry.getKey());
			}
		}
		fsmWindowMap.entrySet().forEach(entry -> {
			FsmWindow window = entry.getValue();
			if (window.isVisible()) {
				window.data.graph = DotPrinter.printToString(window.data.fsm);
				window.graphView.update();
			}
		});
	}

	public void clear() {
		fsmWindowMap.clear();
		desktop.removeAll();
	}

	public void addStateMachine(StateMachine<?, ?> fsm) {
		FsmWindow fsmWindow = fsmWindowMap.get(fsm);
		if (fsmWindow == null) {
			fsmWindow = new FsmWindow(fsm);
			fsmWindowMap.put(fsm, fsmWindow);
			desktop.add(fsmWindow);
			fsmWindow.setVisible(true);
		}
	}
}