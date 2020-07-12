package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.util.Formatting.seconds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.statemachine.core.State;
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
	private JLabel lblGameState;
	private JCheckBox cbShowRoutes;
	private JCheckBox cbShowStates;
	private JCheckBox cbShowGrid;
	private JPanel checkBoxesPanel;
	private GhostHouseStateView ghostHouseStateView;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[][][grow][][][grow,top]"));

		lblGameState = new JLabel("Game Controller State");
		lblGameState.setForeground(Color.BLUE);
		lblGameState.setFont(new Font("SansSerif", Font.BOLD, 16));
		content.add(lblGameState, "cell 0 0,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 1,growx,aligny top");

		table = new GameStateTable();
		table.setRowHeight(24);
		table.setPreferredScrollableViewportSize(new Dimension(450, 350));
		scrollPane.setViewportView(table);

		ghostHouseStateView = new GhostHouseStateView();
		content.add(ghostHouseStateView, "cell 0 2,grow");

		checkBoxesPanel = new JPanel();
		content.add(checkBoxesPanel, "cell 0 4,growx");

		cbShowRoutes = new JCheckBox("Show Routes");
		checkBoxesPanel.add(cbShowRoutes);
		cbShowRoutes.addActionListener(e -> gameController.setShowingRoutes(cbShowRoutes.isSelected()));

		cbShowGrid = new JCheckBox("Show Grid");
		checkBoxesPanel.add(cbShowGrid);
		cbShowGrid.addActionListener(e -> gameController.setShowingGrid(cbShowGrid.isSelected()));

		cbShowStates = new JCheckBox("Show States and Timers");
		checkBoxesPanel.add(cbShowStates);
		cbShowStates.addActionListener(e -> gameController.setShowingStates(cbShowStates.isSelected()));
	}

	/**
	 * Attaches this view to the game controller.
	 * 
	 * @param gameController the game controller
	 */
	public void attachTo(GameController gameController, ArcadeWorldFolks folks) {
		this.gameController = gameController;
		ghostHouseStateView.attachTo(gameController, folks);
		init();
	}

	@Override
	public void init() {
		createTableModel();
	}

	private void createTableModel() {
		gameController.game().ifPresent(game -> {
			GameStateTableModel model = new GameStateTableModel(gameController);
			table.setModel(model);
		});
	}

	@Override
	public void update() {
		if (gameController != null) {
			GameStateTableModel tableModel = (GameStateTableModel) table.getModel();
			if (!tableModel.hasGame()) {
				createTableModel();
			}
			gameController.game().ifPresent(game -> {
				table.update();
				ghostHouseStateView.update();
				updateStateLabel();
				cbShowRoutes.setSelected(gameController.isShowingRoutes());
				cbShowGrid.setSelected(gameController.isShowingGrid());
				cbShowStates.setSelected(gameController.isShowingStates());
			});
		}
	}

	private void updateStateLabel() {
		State<?> state = gameController.state();
		int remaining = state.getTicksRemaining(), duration = state.getDuration();
		String stateText = duration != Integer.MAX_VALUE
				? String.format("%s (%s of %s sec remaining)", state.id(), seconds(remaining), seconds(duration))
				: state.id().toString();
		lblGameState.setText(stateText);
	}
}