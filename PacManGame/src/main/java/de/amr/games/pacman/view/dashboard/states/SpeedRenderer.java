package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.Formatting.pixelsPerSec;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ActorRow;

/**
 * Formats actor speed in pixels/sec and highlights Blinky's speed cell if Blinky is in Elroy mode
 * and is faster than Pac-Man.
 * 
 * @author Armin Reichert
 */
class SpeedRenderer extends DefaultTableCellRenderer {

	private final GameStateTableModel model;

	public SpeedRenderer(GameStateTableModel model) {
		this.model = model;
	}

	private float pacManSpeed() {
		return model.record(ActorRow.PacMan).speed;
	}

	private boolean isBlinkyInsane() {
		Sanity sanity = model.record(ActorRow.Blinky).ghostSanity;
		return sanity == Sanity.CRUISE_ELROY1 || sanity == Sanity.CRUISE_ELROY2;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		float speed = (float) value;
		setText(pixelsPerSec(speed));
		if (row == ActorRow.Blinky.ordinal() && isBlinkyInsane() && speed >= pacManSpeed()) {
			setBackground(new Color(255, 0, 0, 100));
		} else {
			setBackground(table.getBackground());
		}
		return this;
	}
}