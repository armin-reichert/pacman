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

import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import javax.swing.JTable;
import javax.swing.SwingConstants;
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
		format(ColumnInfo.TILE, tileFmt);

		UniversalFormatter speedFmt = new UniversalFormatter();
		speedFmt.fnHilightCondition = c -> c.row == ROW_BLINKY && record(ROW_PACMAN).speed <= record(ROW_BLINKY).speed;
		format(ColumnInfo.SPEED, speedFmt);

		UniversalFormatter ticksFmt = new UniversalFormatter();
		ticksFmt.horizontalAlignment = SwingConstants.TRAILING;
		ticksFmt.fnTextFormat = c -> Formatting.ticksAndSeconds((Long) c.value);
		format(ColumnInfo.REMAINING, ticksFmt);
		format(ColumnInfo.DURATION, ticksFmt);
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