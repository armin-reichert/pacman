package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY1;
import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY2;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.settings.GameStateTableModel.Row;

public class SpeedCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		GameStateTableModel model = (GameStateTableModel) table.getModel();
		float speed = (float) value;
		setText(Formatting.pixelsPerSec(speed));
		Game game = model.gameController.game;
		if (row == Row.Blinky.ordinal() && game.blinky.sanity.is(CRUISE_ELROY1, CRUISE_ELROY2)) {
			setBackground(new Color(255, 0, 0, 100));
		} else {
			setBackground(table.getBackground());
		}
		return this;
	};
}