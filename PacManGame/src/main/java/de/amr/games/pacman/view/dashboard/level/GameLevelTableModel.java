package de.amr.games.pacman.view.dashboard.level;

import static de.amr.games.pacman.view.dashboard.Formatting.integer;
import static de.amr.games.pacman.view.dashboard.Formatting.percent;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.GameLevel;

/**
 * Model of the table displaying all level-dependent parameters.
 * 
 * @author Armin Reichert
 */
public class GameLevelTableModel extends AbstractTableModel {

	//@formatter:off
	static final String[] LEVEL_PARAMS = { 
			"Level", 
			"Pellets Eaten", 
			"Ghosts Killed in Sequence", 
			"Ghosts Killed in Level",
			// from here all are constant per level
			"Bonus", 
			"Bonus value", 
			"Pac-Man Speed", 
			"Pac-Man Dots Speed", 
			"Ghost Speed", 
			"Ghost Tunnel Speed",
			"Elroy 1 Dots Left", 
			"Elroy 1 Speed", 
			"Elroy 2 Dots Left", 
			"Elroy 2 Speed", 
			"Pac-Man Power Speed",
			"Pac-Man Power Dots Speed", 
			"Ghost Frightened Speed", 
			"Pac-Man Power (seconds)", 
			"Number of Flashes", 
		};
		//@formatter:on

	private Game game;

	public GameLevelTableModel() {
		// no game, empty model
	}

	public GameLevelTableModel(Game game) {
		this.game = game;
	}

	public boolean hasGame() {
		return game != null;
	}

	@Override
	public int getRowCount() {
		return game != null ? LEVEL_PARAMS.length : 0;
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
		if (col == 0) {
			return LEVEL_PARAMS[row];
		}
		if (col == 1) {
			return game != null ? levelValue(game.level, row) : null;
		}
		throw new IllegalArgumentException("Illegal column index; " + col);
	}

	private String levelValue(GameLevel level, int row) {
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