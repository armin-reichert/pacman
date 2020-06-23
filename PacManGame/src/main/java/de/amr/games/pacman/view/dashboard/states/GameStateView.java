package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.Formatting.seconds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.GameController;
import net.miginfocom.swing.MigLayout;

/**
 * Displays information (state, timer values, directions, speed) about the actors and the global
 * game controller.
 * 
 * @author Armin Reichert
 */
public class GameStateView extends JPanel implements Lifecycle {

	private GameController gameController;
	private GameStateTable table;
	private JLabel lblGameControllerState;
	private JCheckBox cbShowRoutes;
	private JCheckBox cbShowStates;
	private GhostHouseStateView ghostHouseStateView;
	private JPanel panel;
	private JCheckBox cbShowGrid;
	private JPanel checkBoxesPanel;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[][][][][grow,top]"));

		lblGameControllerState = new JLabel("Game Controller State");
		lblGameControllerState.setForeground(Color.BLUE);
		lblGameControllerState.setFont(new Font("SansSerif", Font.BOLD, 16));
		content.add(lblGameControllerState, "cell 0 0,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 1,growx,aligny top");

		table = new GameStateTable();
		table.setRowHeight(24);
		table.setPreferredScrollableViewportSize(new Dimension(450, 350));
		scrollPane.setViewportView(table);

		panel = new JPanel();
		panel.setBorder(
				new TitledBorder(null, "Ghost House", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
		content.add(panel, "cell 0 2,growx,aligny top");
		panel.setLayout(new MigLayout("", "[grow]", "[grow]"));

		ghostHouseStateView = new GhostHouseStateView();
		panel.add(ghostHouseStateView, "cell 0 0,growx,aligny top");

		checkBoxesPanel = new JPanel();
		content.add(checkBoxesPanel, "cell 0 3,growx");

		cbShowRoutes = new JCheckBox("Show Routes");
		checkBoxesPanel.add(cbShowRoutes);
		cbShowRoutes.addActionListener(e -> gameController.setShowingActorRoutes(cbShowRoutes.isSelected()));

		cbShowGrid = new JCheckBox("Show Grid");
		checkBoxesPanel.add(cbShowGrid);
		cbShowGrid.addActionListener(e -> gameController.setShowingGrid(cbShowGrid.isSelected()));

		cbShowStates = new JCheckBox("Show States and Counters");
		checkBoxesPanel.add(cbShowStates);
		cbShowStates.addActionListener(e -> gameController.setShowingStates(cbShowStates.isSelected()));
	}

	/**
	 * Attaches this view to the game controller.
	 * 
	 * @param gameController the game controller
	 */
	public void attachTo(GameController gameController) {
		this.gameController = gameController;
		table.setModel(new GameStateTableModel(gameController));
		ghostHouseStateView.attachTo(gameController);
		update();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		String stateText = gameController.getState().name();
		if (gameController.state().getDuration() != Integer.MAX_VALUE) {
			stateText = String.format("%s (%s sec of %s sec remaining)", gameController.getState(),
					seconds(gameController.state().getTicksRemaining()), seconds(gameController.state().getDuration()));
		}
		lblGameControllerState.setText(stateText);
		table.update();
		ghostHouseStateView.updateViewState();
		cbShowRoutes.setSelected(gameController.isShowingActorRoutes());
		cbShowGrid.setSelected(gameController.isShowingGrid());
		cbShowStates.setSelected(gameController.isShowingStates());
	}
}