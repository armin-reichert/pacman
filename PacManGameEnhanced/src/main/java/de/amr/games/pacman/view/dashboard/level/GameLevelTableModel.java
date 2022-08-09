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
package de.amr.games.pacman.view.dashboard.level;

import static de.amr.games.pacman.controller.game.GameController.theGame;
import static de.amr.games.pacman.view.dashboard.util.Formatting.integer;
import static de.amr.games.pacman.view.dashboard.util.Formatting.percent;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.game.GameController;

/**
 * Model of the table displaying all level-dependent parameters.
 * 
 * @author Armin Reichert
 */
public class GameLevelTableModel extends AbstractTableModel {

	//@formatter:off
	static final String[] LEVEL_PARAMS = { 
			"Level",
			"Total number of pellets",
			"Pellets Eaten",
			"Pellets Remaining",
			"Ghosts Killed in Sequence", 
			"Ghosts Killed in Level",
			// from here all are constant per level
			"Bonus", 
			"Bonus value", 
			"Pac-Man Speed", 
			"Ghost Speed", 
			"Ghost Tunnel Speed",
			"Elroy 1 Dots Left", 
			"Elroy 1 Speed", 
			"Elroy 2 Dots Left", 
			"Elroy 2 Speed", 
			"Pac-Man Power Speed",
			"Ghost Frightened Speed", 
			"Pac-Man Power (seconds)", 
			"Number of Flashes", 
		};
		//@formatter:on

	@Override
	public int getRowCount() {
		return GameController.isGameStarted() ? LEVEL_PARAMS.length : 0;
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
			return GameController.isGameStarted() ? levelValue(row) : null;
		}
		throw new IllegalArgumentException("Illegal column index; " + col);
	}

	private String levelValue(int row) {
		switch (row) {
		case 0:
			return integer(theGame.level);
		case 1:
			return integer(theGame.foodCount);
		case 2:
			return integer(theGame.eatenFoodCount);
		case 3:
			return integer(theGame.remainingFoodCount());
		case 4:
			return integer(theGame.ghostsKilledByEnergizer);
		case 5:
			return integer(theGame.ghostsKilledInLevel);
		case 6:
			return theGame.bonusSymbol;
		case 7:
			return integer(theGame.bonusValue);
		case 8:
			return percent(theGame.pacManSpeed);
		case 9:
			return percent(theGame.ghostSpeed);
		case 10:
			return percent(theGame.ghostTunnelSpeed);
		case 11:
			return integer(theGame.elroy1DotsLeft);
		case 12:
			return percent(theGame.elroy1Speed);
		case 13:
			return integer(theGame.elroy2DotsLeft);
		case 14:
			return percent(theGame.elroy2Speed);
		case 15:
			return percent(theGame.pacManPowerSpeed);
		case 16:
			return percent(theGame.ghostFrightenedSpeed);
		case 17:
			return integer(theGame.pacManPowerSeconds);
		case 18:
			return integer(theGame.numFlashes);
		default:
			return null;
		}
	}
}