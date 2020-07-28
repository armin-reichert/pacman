package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ColumnInfo;
import de.amr.games.pacman.view.dashboard.util.Formatting;
import de.amr.games.pacman.view.dashboard.util.UniversalFormatter;

/**
 * Displays detailed information about the actors in the game.
 * 
 * @author Armin Reichert
 */
public class GameStateTable extends JTable implements Lifecycle {

	public GameStateTable() {
		super(new GameStateTableModel());
		setRowSelectionAllowed(false);
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		init();
	}

	@Override
	public void init() {
		UniversalFormatter tileFmt = new UniversalFormatter();
		tileFmt.fnHilightCondition = c -> record(c.row) != null && record(c.row).pacManCollision;
		format(ColumnInfo.Tile, tileFmt);

		UniversalFormatter speedFmt = new UniversalFormatter();
		speedFmt.fnHilightCondition = c -> c.row == ROW_BLINKY && record(ROW_PACMAN).speed <= record(ROW_BLINKY).speed;
		format(ColumnInfo.Speed, speedFmt);

		UniversalFormatter ticksFmt = new UniversalFormatter();
		ticksFmt.fnTextFormat = c -> Formatting.ticksAndSeconds((Long) c.value);
		format(ColumnInfo.Remaining, ticksFmt);
		format(ColumnInfo.Duration, ticksFmt);
	}

	@Override
	public void update() {
		getGameStateTableModel().update();
	}

	private void format(ColumnInfo columnInfo, TableCellRenderer renderer) {
		getColumnModel().getColumn(columnInfo.ordinal()).setCellRenderer(renderer);
	}

	private GameStateTableModel getGameStateTableModel() {
		return (GameStateTableModel) getModel();
	}

	private GameStateRecord record(int row) {
		return getGameStateTableModel().record(row);
	}
}