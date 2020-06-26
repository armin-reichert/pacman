package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ColumnInfo;
import de.amr.games.pacman.view.dashboard.util.UniversalFormatter;
import de.amr.games.pacman.view.dashboard.util.Formatting;

/**
 * Displays detailed information about the actors in the game that is updated at every tick.
 * 
 * @author Armin Reichert
 */
public class GameStateTable extends JTable {

	public GameStateTable() {
		super(new GameStateTableModel());
		setRowSelectionAllowed(false);
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		configureRenderers();
	}

	public void update() {
		getGameStateTableModel().update();
	}

	private void configureRenderers() {
		UniversalFormatter tileFormatter = new UniversalFormatter();
		tileFormatter.fnHilightCondition = c -> record(c.row) != null && record(c.row).pacManCollision;
		renderer(ColumnInfo.Tile, tileFormatter);

		UniversalFormatter speedFormatter = new UniversalFormatter();
		speedFormatter.fnHilightCondition = c -> c.row == ROW_BLINKY && record(ROW_PACMAN).speed <= record(ROW_BLINKY).speed;
		renderer(ColumnInfo.Speed, speedFormatter);

		UniversalFormatter ticksFormatter = new UniversalFormatter();
		ticksFormatter.fnTextFormat = c -> Formatting.ticksAndSeconds((int) c.value);
		renderer(ColumnInfo.Remaining, ticksFormatter);
		renderer(ColumnInfo.Duration, ticksFormatter);
	}

	private void renderer(ColumnInfo columnInfo, TableCellRenderer renderer) {
		getColumnModel().getColumn(columnInfo.ordinal()).setCellRenderer(renderer);
	}

	private GameStateTableModel getGameStateTableModel() {
		return (GameStateTableModel) getModel();
	}

	private GameStateRecord record(int row) {
		return getGameStateTableModel().record(row);
	}
}