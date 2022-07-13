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
import javax.swing.SwingConstants;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.game.ExtendedGameController;
import de.amr.games.pacman.controller.game.PacManGameState;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.statemachine.core.State;
import net.miginfocom.swing.MigLayout;

/**
 * Displays information (state, timer values, directions, speed) about the actors and the global game controller.
 * 
 * @author Armin Reichert
 */
public class GameStateView extends JPanel implements Lifecycle {

	private ExtendedGameController gameController;
	private GameStateTable table;
	private JCheckBox cbShowRoutes;
	private JCheckBox cbShowStates;
	private JCheckBox cbShowGrid;
	private JPanel checkBoxesPanel;
	private GhostHouseStateView ghostHouseStateView;
	private JLabel lblGameState;
	private JCheckBox cbDemoMode;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[][][][grow,top]"));

		lblGameState = new JLabel("Game State");
		lblGameState.setForeground(Color.BLUE);
		lblGameState.setHorizontalAlignment(SwingConstants.CENTER);
		lblGameState.setFont(new Font("SansSerif", Font.BOLD, 18));
		content.add(lblGameState, "cell 0 0,growx");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMaximumSize(new Dimension(32767, 200));
		content.add(scrollPane, "cell 0 1,growx,aligny top");

		table = new GameStateTable();
		table.setRowHeight(24);
		table.setPreferredScrollableViewportSize(new Dimension(450, 350));
		scrollPane.setViewportView(table);

		ghostHouseStateView = new GhostHouseStateView();
		content.add(ghostHouseStateView, "cell 0 2,grow");

		checkBoxesPanel = new JPanel();
		content.add(checkBoxesPanel, "flowx,cell 0 3,alignx left");

		cbShowRoutes = new JCheckBox("Show Routes");
		checkBoxesPanel.add(cbShowRoutes);
		cbShowRoutes.addActionListener(e -> gameController.setShowingRoutes(cbShowRoutes.isSelected()));

		cbShowGrid = new JCheckBox("Show Grid");
		checkBoxesPanel.add(cbShowGrid);
		cbShowGrid.addActionListener(e -> gameController.setShowingGrid(cbShowGrid.isSelected()));

		cbShowStates = new JCheckBox("Show States and Timers");
		checkBoxesPanel.add(cbShowStates);

		cbDemoMode = new JCheckBox("Demo Mode");
		checkBoxesPanel.add(cbDemoMode);
		cbDemoMode.addActionListener(e -> gameController.toggleDemoMode());
		cbShowStates.addActionListener(e -> gameController.setShowingStates(cbShowStates.isSelected()));
	}

	/**
	 * Attaches this view to the game controller.
	 * 
	 * @param gameController the game controller
	 */
	public void attachTo(ExtendedGameController gameController, Folks folks) {
		this.gameController = gameController;
		ghostHouseStateView.attachTo(gameController, folks);
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void update() {
		if (gameController != null) {
			if (PacManGame.started()) {
				GameStateTableModel tableModel = (GameStateTableModel) table.getModel();
				if (tableModel.isDummy()) {
					table.setModel(new GameStateTableModel(gameController));
				}
			}
			table.update();
			ghostHouseStateView.update();
			setStateLabel();
			cbShowRoutes.setSelected(gameController.isShowingRoutes());
			cbShowGrid.setSelected(gameController.isShowingGrid());
			cbShowStates.setSelected(gameController.isShowingStates());
			cbDemoMode.setSelected(PacManApp.appSettings.demoMode);
		}
	}

	private void setStateLabel() {
		State<PacManGameState> state = gameController.state();
		if (state.hasTimer()) {
			long remaining = state.getTicksRemaining(), duration = state.getDuration();
			lblGameState
					.setText(String.format("%s (%s of %s sec remaining)", state.id(), seconds(remaining), seconds(duration)));
		} else {
			lblGameState.setText(state.id().name());
		}
	}
}