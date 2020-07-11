package de.amr.games.pacman.view.dashboard.theme;

import java.awt.event.ItemEvent;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.view.api.PacManGameView;
import net.miginfocom.swing.MigLayout;

public class ThemeSelectionView extends JPanel implements Lifecycle {

	private GameController gameController;
	private JComboBox<String> comboSelectTheme;

	public void attachTo(GameController gameController) {
		this.gameController = gameController;
	}

	public ThemeSelectionView() {
		setLayout(new MigLayout("", "[][grow]", "[]"));

		JLabel lblSelectTheme = new JLabel("Select Theme");
		add(lblSelectTheme, "cell 0 0,alignx trailing");

		comboSelectTheme = new JComboBox<>();
		comboSelectTheme.setModel(new DefaultComboBoxModel<String>(new String[] { "ARCADE", "LETTERS", "BLOCKS" }));
		add(comboSelectTheme, "cell 1 0,alignx left");
		comboSelectTheme.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String themeName = comboSelectTheme.getModel().getElementAt(comboSelectTheme.getSelectedIndex());
				gameController.selectTheme(themeName);
			}
		});
	}

	private Optional<View> currentView() {
		return Optional.ofNullable(gameController).flatMap(GameController::currentView);
	}

	private void updateSelectionFromTheme() {
		currentView().ifPresent(view -> {
			PacManGameView gameView = (PacManGameView) view;
			comboSelectTheme.setSelectedItem(gameView.getTheme().name().toUpperCase());
		});
	}

	@Override
	public void init() {
		updateSelectionFromTheme();
	}

	@Override
	public void update() {
		updateSelectionFromTheme();
	}
}