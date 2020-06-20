package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.sec;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

public class GameStateViewModel extends AbstractTableModel {

	static class Data {
		String name;
		String state;
		int ticksRemaining;
		int duration;
		Tile tile;
		Tile target;
		boolean pacManCollision;

		public Data(Game game, PacMan pacMan) {
			name = "Pac-Man";
			state = pacMan.power == 0 ? pacMan.getState().name() : "POWER";
			ticksRemaining = pacMan.power == 0 ? pacMan.state().getTicksRemaining() : pacMan.power;
			duration = pacMan.power == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
			tile = pacMan.tile();
		}

		public Data(Game game, GhostCommand ghostCommand, Ghost ghost) {
			name = ghost.name;
			state = ghost.getState().name();
			ticksRemaining = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
					: ghost.state().getTicksRemaining();
			duration = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration() : ghost.state().getDuration();
			tile = ghost.tile();
			target = ghost.targetTile();
			pacManCollision = tile.equals(game.pacMan.tile());
		}
	}

	static GameStateViewModel SAMPLE_DATA = new GameStateViewModel() {

		//@formatter:off
		Object[][] data = {
				{ "Blinky", Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ "Pinky", Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ "Inky", Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ "Clyde", Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ "Pac-Man", Tile.at(0, 0), Tile.at(0, 0), PacManState.SLEEPING, 0, 0, false },
		};
		//@formatter:on

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
	};

	public enum Columns {
		Name, Tile, Target, State, Remaining, Duration;
	};

	static final int NUM_ROWS = 5;

	public GameController gameController;
	public Game game;
	public GhostCommand ghostCommand;
	public Data[] data = new Data[NUM_ROWS];

	private GameStateViewModel() {
	}

	public GameStateViewModel(GameController gameController) {
		this.gameController = gameController;
		game = gameController.game;
		ghostCommand = gameController.ghostCommand;
	}

	@Override
	public int getRowCount() {
		return NUM_ROWS;
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}

	@Override
	public String getColumnName(int col) {
		return Columns.values()[col].name();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return super.getColumnClass(col);
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (Columns.values()[col]) {
		case Name:
			return data[row].name;
		case Tile:
			return data[row].tile != null ? data[row].tile : "";
		case Target:
			return data[row].target != null ? data[row].target : "";
		case State:
			return data[row].state;
		case Remaining:
			return data[row].ticksRemaining;
		case Duration:
			return data[row].duration;
		default:
			return null;
		}
	}

	public void update() {
		for (int row = 0; row < NUM_ROWS; ++row) {
			computeRow(row);
		}
		fireTableDataChanged();
	}

	public void computeRow(int row) {
		data[row] = (row == 4) ? new Data(game, game.pacMan) : new Data(game, ghostCommand, ghost(row));
	}

	private Ghost ghost(int i) {
		switch (i) {
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