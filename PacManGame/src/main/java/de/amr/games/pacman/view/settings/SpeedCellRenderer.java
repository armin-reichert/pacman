package de.amr.games.pacman.view.settings;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.view.settings.GameStateViewModel.Row;

public class SpeedCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		GameStateViewModel model = (GameStateViewModel) table.getModel();
		float speed = (float) value;
		setText(Formatting.pixelsPerSec(speed));
		if (row == Row.Blinky.ordinal() && model.game.blinky.sanity.is(Sanity.CRUISE_ELROY1, Sanity.CRUISE_ELROY2)) {
			setBackground(new Color(255, 0, 0, 100));
			setToolTipText(model.game.blinky.sanity.getState().name());
		} else {
			setBackground(table.getBackground());
		}
		return this;
	};
}