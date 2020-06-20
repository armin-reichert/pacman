package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.sec;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

public class GameStateViewModel extends AbstractTableModel {

	public enum Column {
		//@formatter:off
		OnStage(Boolean.class, true), 
		Name(String.class, false), 
		Tile(Tile.class, false), 
		Target(Tile.class, false),
		MoveDir(Direction.class, false),
		WishDir(Direction.class, false),
		State(Object.class, false), 
		Remaining(Integer.class, false), 
		Duration(Integer.class, false);
		//@formatter:on

		private Column(Class<?> class_, boolean editable) {
			this.class_ = class_;
			this.editable = editable;
		}

		public Class<?> class_;
		public boolean editable;

		public static Column at(int col) {
			return Column.values()[col];
		}
	};

	static final int NUM_ROWS = 5;
	static final int PACMAN_ROW = 4;

	static class Data {
		boolean onStage;
		String name;
		Direction moveDir;
		Direction wishDir;
		String state;
		int ticksRemaining;
		int duration;
		Tile tile;
		Tile target;
		boolean pacManCollision;

		public Data(Game game, PacMan pacMan) {
			onStage = game.onStage(pacMan);
			name = "Pac-Man";
			moveDir = pacMan.moveDir();
			wishDir = pacMan.wishDir();
			state = pacMan.power == 0 ? pacMan.getState().name() : "POWER";
			ticksRemaining = pacMan.power == 0 ? pacMan.state().getTicksRemaining() : pacMan.power;
			duration = pacMan.power == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
			tile = pacMan.tile();
		}

		public Data(Game game, GhostCommand ghostCommand, Ghost ghost) {
			onStage = game.onStage(ghost);
			name = ghost.name;
			moveDir = ghost.moveDir();
			wishDir = ghost.wishDir();
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
				{ true, "Blinky", Direction.DOWN, Direction.LEFT, Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ false, "Pinky", Direction.DOWN, Direction.LEFT, Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ true, "Inky", Direction.DOWN, Direction.LEFT, Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ true, "Clyde", Direction.DOWN, Direction.LEFT, Tile.at(0, 0), Tile.at(0, 0), GhostState.LOCKED, 0, 0, false },
				{ true, "Pac-Man", Direction.DOWN, Direction.LEFT, Tile.at(0, 0), Tile.at(0, 0), PacManState.SLEEPING, 0, 0, false },
		};
		//@formatter:on

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
	};

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
		return Column.values().length;
	}

	@Override
	public String getColumnName(int col) {
		return Column.at(col).name();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return Column.at(col).class_;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return Column.at(col).editable && row != PACMAN_ROW;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (data[row] == null) {
			return null;
		}
		switch (Column.at(col)) {
		case OnStage:
			return data[row].onStage;
		case Name:
			return data[row].name;
		case MoveDir:
			return data[row].moveDir;
		case WishDir:
			return data[row].wishDir;
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

	@Override
	public void setValueAt(Object value, int row, int col) {
		Column column = Column.at(col);
		if (column == Column.OnStage && row != PACMAN_ROW) {
			boolean onStage = (boolean) value;
			Creature<?> creature = ghost(row);
			if (onStage) {
				game.putOnStage(creature);
			} else {
				game.pullFromStage(creature);
			}
			data[row].onStage = (boolean) value;
			fireTableDataChanged();
		}
	}

	public void update() {
		for (int row = 0; row < NUM_ROWS; ++row) {
			data[row] = (row == PACMAN_ROW) ? new Data(game, game.pacMan) : new Data(game, ghostCommand, ghost(row));
		}
		fireTableDataChanged();
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