package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Data model of the table displaying actor data.
 * 
 * @author Armin Reichert
 */
public class GameStateTableModel extends AbstractTableModel {

	public enum Column {
		//@formatter:off
		OnStage("On Stage", Boolean.class, true), 
		Name("Actor", String.class, false), 
		Tile(Tile.class, false), 
		Target(Tile.class, false),
		MoveDir(Direction.class, false),
		WishDir(Direction.class, false),
		Speed("Pixel/sec", Float.class, false),
		State(Object.class, false), 
		Remaining(Integer.class, false), 
		Duration(Integer.class, false);
		//@formatter:on

		private Column(Class<?> class_, boolean editable) {
			this.name = name();
			this.class_ = class_;
			this.editable = editable;
		}

		private Column(String name, Class<?> class_, boolean editable) {
			this.name = name;
			this.class_ = class_;
			this.editable = editable;
		}

		public String name;
		public Class<?> class_;
		public boolean editable;

		public static Column at(int col) {
			return Column.values()[col];
		}
	};

	public enum Row {
		Blinky, Pinky, Inky, Clyde, PacMan, Bonus
	}

	// sample data for window builder
	static final GameStateTableModel SAMPLE_DATA = new GameStateTableModel() {

		//@formatter:off
		Object[][] data = {
		{ true,  "Blinky",  Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, 0f, LOCKED,    0, 0, false },
		{ false, "Pinky",   Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, 0f, LOCKED,    0, 0, false },
		{ true,  "Inky",    Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, 0f, LOCKED,    0, 0, false },
		{ true,  "Clyde",   Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, 0f, LOCKED,    0, 0, false },
		{ true,  "Pac-Man", Tile.at(0, 0), null,          DOWN, LEFT, 0f, SLEEPING,  0, 0, false },
		{ true,  "Bonus",   Tile.at(0, 0), null,          null, null, 0f, INACTIVE,  0, 0, false },
		};
		//@formatter:on

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
	};

	public GameController gameController;
	public ActorData[] data = new ActorData[Row.values().length];
	public Ghost[] ghostByRow;

	private GameStateTableModel() {
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		Game game = gameController.game;
		ghostByRow = new Ghost[] { game.blinky, game.pinky, game.inky, game.clyde };
	}

	@Override
	public int getRowCount() {
		return Row.values().length;
	}

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}

	@Override
	public String getColumnName(int col) {
		return Column.at(col).name;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return Column.at(col).class_;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return Column.at(col).editable && row < Row.PacMan.ordinal();
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
			return data[row].tile;
		case Target:
			return data[row].target;
		case Speed:
			return data[row].speed;
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
		if (column == Column.OnStage) {
			data[row].onStage = (boolean) value;
			fireTableCellUpdated(row, col);
		}
	}
}