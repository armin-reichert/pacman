package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.view.settings.Formatting.integer;
import static de.amr.games.pacman.view.settings.Formatting.percent;

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
		return "";
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
		case 10:
			return "Elroy1 Dots Left";
		case 11:
			return "Elroy1 Speed";
		case 12:
			return "Elroy2 Dots Left";
		case 13:
			return "Elroy2 Speed";
		case 14:
			return "Pac-Man Power Speed";
		case 15:
			return "Pac-Man Power Dots Speed";
		case 16:
			return "Ghost Frightened Speed";
		case 17:
			return "Pac-Man Power (seconds)";
		case 18:
			return "Number of Flashes";
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