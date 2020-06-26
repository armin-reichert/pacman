package de.amr.games.pacman.view.dashboard.states;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Data model of the table displaying actor data.
 * 
 * @author Armin Reichert
 */
class GameStateTableModel extends AbstractTableModel {

	public static final int NUM_ROWS = 6;

	public static final int ROW_BLINKY = 0;
	public static final int ROW_PINKY = 1;
	public static final int ROW_INKY = 2;
	public static final int ROW_CLYDE = 3;
	public static final int ROW_PACMAN = 4;
	public static final int ROW_BONUS = 5;

	public enum ColumnInfo {
		//@formatter:off
		OnStage(null, Boolean.class, true), 
		Name("Actor", String.class, false), 
		Tile(null, Tile.class, false), 
		Target(null, Tile.class, false),
		MoveDir("Moves", Direction.class, false),
		WishDir("Wants", Direction.class, false),
		Speed("px/s", Float.class, false),
		State(null, Object.class, false),
		Sanity(null, Sanity.class, false),
		Remaining(null, Integer.class, false), 
		Duration(null, Integer.class, false);
		//@formatter:on

		private ColumnInfo(String name, Class<?> class_, boolean editable) {
			this.name = name != null ? name : name();
			this.class_ = class_;
			this.editable = editable;
		}

		public final String name;
		public final Class<?> class_;
		public final boolean editable;

		public static ColumnInfo at(int col) {
			return ColumnInfo.values()[col];
		}
	};

	private GameController gameController;
	private GameStateRecord[] records;

	public GameStateTableModel() {
		records = createRecords();
		for (int i = 0; i < records.length; ++i) {
			records[i] = new GameStateRecord();
		}
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		records = createRecords();
		addTableModelListener(e -> {
			if (e.getColumn() == ColumnInfo.OnStage.ordinal()) {
				int row = e.getFirstRow();
				GameStateRecord r = records[row];
				if (r.creature instanceof Ghost) {
					gameController.game.takePart(r.creature, r.takesPart);
				}
			}
		});
	}

	private GameStateRecord[] createRecords() {
		GameStateRecord[] records = new GameStateRecord[NUM_ROWS];
		for (int i = 0; i < records.length; ++i) {
			records[i] = new GameStateRecord();
		}
		records[ROW_BLINKY].name = "Blinky";
		records[ROW_PINKY].name = "Pinky";
		records[ROW_INKY].name = "Inky";
		records[ROW_CLYDE].name = "Clyde";
		records[ROW_PACMAN].name = "Pac-Man";
		records[ROW_BONUS].name = "Bonus";
		return records;
	}

	public GameController getGameController() {
		return gameController;
	}

	public boolean hasGame() {
		return gameController != null && gameController.game != null;
	}

	public void update() {
		if (gameController != null) {
			Game game = gameController.game;
			GhostCommand ghostCommand = gameController.ghostCommand;
			records[ROW_BLINKY] = new GameStateRecord(game, ghostCommand, game.blinky);
			records[ROW_PINKY] = new GameStateRecord(game, ghostCommand, game.pinky);
			records[ROW_INKY] = new GameStateRecord(game, ghostCommand, game.inky);
			records[ROW_CLYDE] = new GameStateRecord(game, ghostCommand, game.clyde);
			records[ROW_PACMAN] = new GameStateRecord(game, game.pacMan);
			records[ROW_BONUS] = new GameStateRecord(game, game.bonus);
			fireTableDataChanged();
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		GameStateRecord r = records[row];
		switch (ColumnInfo.at(col)) {
		case OnStage:
			return r.takesPart;
		case Name:
			return r.name;
		case MoveDir:
			return r.moveDir;
		case WishDir:
			return r.wishDir;
		case Tile:
			return r.tile;
		case Target:
			return r.target;
		case Speed:
			return r.speed;
		case State:
			return r.state;
		case Remaining:
			return r.ticksRemaining;
		case Duration:
			return r.duration;
		case Sanity:
			return r.ghostSanity;
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		switch (ColumnInfo.at(col)) {
		case OnStage:
			records[row].takesPart = (boolean) value;
			fireTableCellUpdated(row, col);
			break;
		default:
			break;
		}
	}

	public GameStateRecord record(int row) {
		return records[row];
	}

	@Override
	public int getRowCount() {
		return NUM_ROWS;
	}

	@Override
	public int getColumnCount() {
		return ColumnInfo.values().length;
	}

	@Override
	public String getColumnName(int col) {
		return ColumnInfo.at(col).name;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return ColumnInfo.at(col).class_;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return ColumnInfo.at(col).editable;
	}
}