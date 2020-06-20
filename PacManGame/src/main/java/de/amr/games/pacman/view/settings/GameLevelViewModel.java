package de.amr.games.pacman.view.settings;

import java.text.DecimalFormat;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.GameLevel;

public class GameLevelViewModel extends AbstractTableModel {

	private Game game;

	public GameLevelViewModel(Game game) {
		this.game = game;
	}

	public void update() {
		fireTableCellUpdated(0, 1);
		fireTableCellUpdated(1, 1);
		fireTableCellUpdated(2, 1);
		fireTableCellUpdated(3, 1);
	}

	@Override
	public int getRowCount() {
		return 19;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		return col == 0 ? "Parameter" : "Value";
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return name(row);
		case 1:
			return value(row);
		default:
			return null;
		}
	}

	private String name(int row) {
		switch (row) {
		case 0:
			return "Level";
		case 1:
			return "Pellets Eaten";
		case 2:
			return "Ghosts Killed in Sequence";
		case 3:
			return "Ghosts Killed in Level";
		case 4:
			return "Bonus";
		case 5:
			return "Bonus value";
		case 6:
			return "Pac-Man Speed";
		case 7:
			return "Pac-Man Dots Speed";
		case 8:
			return "Ghost Speed";
		case 9:
			return "Ghost Tunnel Speed";
		default:
			return null;
		}
	}

	private String value(int row) {
		GameLevel level = game.level;
		switch (row) {
		case 0:
			return integer(level.number);
		case 1:
			return integer(level.eatenFoodCount);
		case 2:
			return integer(level.ghostsKilledByEnergizer);
		case 3:
			return integer(level.ghostsKilled);
		case 4:
			return level.bonusSymbol.name();
		case 5:
			return integer(level.bonusValue);
		case 6:
			return percent(level.pacManSpeed);
		case 7:
			return percent(level.pacManDotsSpeed);
		case 8:
			return percent(level.ghostSpeed);
		case 9:
			return percent(level.ghostTunnelSpeed);
		default:
			return null;
		}
	}

	static String integer(int i) {
		return DecimalFormat.getNumberInstance(Locale.ENGLISH).format(i);
	}

	static String percent(float f) {
		return DecimalFormat.getPercentInstance(Locale.ENGLISH).format(f);
	}

}
