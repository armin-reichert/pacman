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

public class GhostStateModel extends AbstractTableModel {

	private static final String[] columnNames = { "Name", "Tile", "State", "Remaining", "Duration" };

	private GameController gameController;
	private Game game;
	private GhostCommand ghostCommand;

	class Data {
		String name;
		String state;
		int ticksRemaining;
		int duration;
		Tile tile;

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
		}

		public Data(GameController controller) {
			name = "GameController";
			state = controller.getState().name();
			ticksRemaining = controller.state().getTicksRemaining();
			duration = controller.state().getDuration();
		}
	}

	public GhostStateModel(GameController gameController) {
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
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Data data = getData(rowIndex);
		switch (columnIndex) {
		case 0:
			return data.name;
		case 1:
			return data.tile != null ? data.tile : "";
		case 2:
			return data.state;
		case 3:
			return data.ticksRemaining == Integer.MAX_VALUE ? Character.toString('\u221E') : data.ticksRemaining;
		case 4:
			return data.duration == Integer.MAX_VALUE ? Character.toString('\u221E') : data.duration;
		default:
			return null;
		}
	}

	public void update() {
		fireTableDataChanged();
	}

	private Data getData(int rowIndex) {
		return rowIndex == 5 ? new Data(gameController) : rowIndex == 4 ? new Data(game.pacMan) : new Data(ghost(rowIndex));
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