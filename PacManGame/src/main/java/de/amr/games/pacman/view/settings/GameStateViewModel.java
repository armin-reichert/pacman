package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.sec;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

public class GameStateViewModel extends AbstractTableModel {

	static final int NUM_ROWS = 6;

	public enum Columns {
		NAME, TILE, STATE, REMAINING, DURATION
	};

	private static final String[] columnNames = { "Name", "Tile", "State", "Remaining", "Duration" };

	private GameController gameController;
	private Game game;
	private GhostCommand ghostCommand;

	public Data[] data = new Data[NUM_ROWS];

	class Data {
		String name;
		String state;
		int ticksRemaining;
		int duration;
		Tile tile;
		boolean pacManCollision;

		public Data(PacMan pacMan) {
			name = "Pac-Man";
			state = pacMan.power == 0 ? pacMan.getState().name() : "POWER";
			ticksRemaining = pacMan.power == 0 ? pacMan.state().getTicksRemaining() : pacMan.power;
			duration = pacMan.power == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
			tile = pacMan.tile();
		}

		public Data(Ghost ghost) {
			name = ghost.name;
			state = ghost.getState().name();
			ticksRemaining = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
					: ghost.state().getTicksRemaining();
			duration = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration() : ghost.state().getDuration();
			tile = ghost.tile();
			pacManCollision = tile.equals(game.pacMan.tile());
		}

		public Data(GameController controller) {
			name = "GameController";
			state = controller.getState().name();
			ticksRemaining = controller.state().getTicksRemaining();
			duration = controller.state().getDuration();
		}
	}

	public GameStateViewModel(GameController gameController) {
		this.gameController = gameController;
		game = gameController.game;
		ghostCommand = gameController.ghostCommand;
	}

	@Override
	public int getRowCount() {
		return 6;
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return super.getColumnClass(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		computeData(rowIndex);
		switch (Columns.values()[columnIndex]) {
		case NAME:
			return data[rowIndex].name;
		case TILE:
			return data[rowIndex].tile != null ? data[rowIndex].tile : "";
		case STATE:
			return data[rowIndex].state;
		case REMAINING:
			return data[rowIndex].ticksRemaining;
		case DURATION:
			return data[rowIndex].duration;
		default:
			return null;
		}
	}

	public void update() {
		fireTableDataChanged();
	}

	public void computeData(int rowIndex) {
		data[rowIndex] = rowIndex == 5 ? new Data(gameController)
				: rowIndex == 4 ? new Data(game.pacMan) : new Data(ghost(rowIndex));
	}

	private Ghost ghost(int index) {
		switch (index) {
		case 0:
			return game.blinky;
		case 1:
			return game.pinky;
		case 2:
			return game.inky;
		case 3:
			return game.clyde;
		default:
			return null;
		}
	}
}