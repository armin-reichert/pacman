package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.view.settings.Formatting.integer;
import static de.amr.games.pacman.view.settings.Formatting.percent;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.GameLevel;

public class GameLevelViewModel extends AbstractTableModel {

	static final String[] names = { "Level", "Pellets Eaten", "Ghosts Killed in Sequence", "Ghosts Killed in Level",
			"Bonus", "Bonus value", "Pac-Man Speed", "Pac-Man Dots Speed", "Ghost Speed", "Ghost Tunnel Speed",
			"Elroy1 Dots Left", "Elroy1 Speed", "Elroy2 Dots Left", "Elroy2 Speed", "Pac-Man Power Speed",
			"Pac-Man Power Dots Speed", "Ghost Frightened Speed", "Pac-Man Power (seconds)", "Number of Flashes", };

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
		return names.length;
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
			return names[row];
		case 1:
			return value(row);
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
		case 10:
			return integer(level.elroy1DotsLeft);
		case 11:
			return percent(level.elroy1Speed);
		case 12:
			return integer(level.elroy2DotsLeft);
		case 13:
			return percent(level.elroy2Speed);
		case 14:
			return percent(level.pacManPowerSpeed);
		case 15:
			return percent(level.pacManPowerDotsSpeed);
		case 16:
			return percent(level.ghostFrightenedSpeed);
		case 17:
			return integer(level.pacManPowerSeconds);
		case 18:
			return integer(level.numFlashes);
		default:
			return null;
		}
	}
}