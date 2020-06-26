package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ColumnInfo;
import de.amr.games.pacman.view.dashboard.util.CellFormatter;
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
		CellFormatter tileRenderer = new CellFormatter();
		tileRenderer.fnHilightCondition = (row, col) -> record(row) != null && record(row).pacManCollision;
		renderer(ColumnInfo.Tile, tileRenderer);

		CellFormatter speedRenderer = new CellFormatter();
		speedRenderer.fnHilightCondition = (row, col) -> row == ROW_BLINKY
				&& record(ROW_PACMAN).speed <= record(ROW_BLINKY).speed;
		renderer(ColumnInfo.Speed, speedRenderer);

		CellFormatter ticksRenderer = new CellFormatter();
		ticksRenderer.fnTextFormat = value -> Formatting.ticksAndSeconds((int) value);
		renderer(ColumnInfo.Remaining, ticksRenderer);
		renderer(ColumnInfo.Duration, ticksRenderer);
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