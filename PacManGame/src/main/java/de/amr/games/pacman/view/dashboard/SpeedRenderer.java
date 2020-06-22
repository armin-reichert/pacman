package de.amr.games.pacman.view.dashboard;

import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY1;
import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY2;
import static de.amr.games.pacman.view.dashboard.Formatting.pixelsPerSec;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.dashboard.GameStateTableModel.ActorRow;

/**
 * Formats actor speed in pixels/sec and highlights Blinky's speed cell if Blinky is in Elroy mode
 * and is faster than Pac-Man.
 * 
 * @author Armin Reichert
 */
public class SpeedRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		GameStateTableModel model = (GameStateTableModel) table.getModel();
		Game game = model.gameController.game;
		float speed = (float) value;
		setText(pixelsPerSec(speed));
		if (row == ActorRow.Blinky.ordinal() && game.blinky.sanity.is(CRUISE_ELROY1, CRUISE_ELROY2)
				&& speed >= model.records[ActorRow.PacMan.ordinal()].speed) {
			setBackground(new Color(255, 0, 0, 100));
		} else {
			setBackground(table.getBackground());
		}
		return this;
	};
}