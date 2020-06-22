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

	public final static GameStateTableModel LOREM_IPSUM = new GameStateTableModel();

	static {
		LOREM_IPSUM.record(ROW_BLINKY).name = "Blinky";
		LOREM_IPSUM.record(ROW_PINKY).name = "Pinky";
		LOREM_IPSUM.record(ROW_INKY).name = "Inky";
		LOREM_IPSUM.record(ROW_CLYDE).name = "Clyde";
		LOREM_IPSUM.record(ROW_PACMAN).name = "Pac-Man";
		LOREM_IPSUM.record(ROW_BONUS).name = "Bonus";
	}

	public enum Field {
		//@formatter:off
		OnStage("On Stage", Boolean.class, true), 
		Name("Actor", String.class, false), 
		Tile(Tile.class, false), 
		Target(Tile.class, false),
		MoveDir(Direction.class, false),
		WishDir(Direction.class, false),
		Speed("Pixel/sec", Float.class, false),
		State(Object.class, false),
		GhostSanity(Sanity.class, false),
		Remaining(Integer.class, false), 
		Duration(Integer.class, false);
		//@formatter:on

		private Field(Class<?> class_, boolean editable) {
			this.name = name();
			this.class_ = class_;
			this.editable = editable;
		}

		private Field(String name, Class<?> class_, boolean editable) {
			this.name = name;
			this.class_ = class_;
			this.editable = editable;
		}

		public String name;
		public Class<?> class_;
		public boolean editable;

		public static Field at(int col) {
			return Field.values()[col];
		}
	};

	public GameController gameController;
	public Ghost[] ghostByRow;
	private final GameStateRecord[] records;

	public GameStateTableModel() {
		records = new GameStateRecord[NUM_ROWS];
		for (int i = 0; i < records.length; ++i) {
			records[i] = new GameStateRecord();
		}
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		records = new GameStateRecord[NUM_ROWS];
		Game game = gameController.game;
		ghostByRow = new Ghost[] { game.blinky, game.pinky, game.inky, game.clyde };
		addTableModelListener(e -> {
			if (e.getColumn() == Field.OnStage.ordinal()) {
				int row = e.getFirstRow();
				if (row != ROW_PACMAN && row != ROW_BONUS) {
					gameController.game.takePart(ghostByRow[row], records[row].takesPart);
				}
			}
		});
	}

	public void update() {
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

	@Override
	public Object getValueAt(int row, int col) {
		GameStateRecord r = records[row];
		switch (Field.at(col)) {
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
		case GhostSanity:
			return r.ghostSanity;
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (Field.at(col) == Field.OnStage) {
			records[row].takesPart = (boolean) value;
			fireTableCellUpdated(row, col);
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
		return Field.values().length;
	}

	@Override
	public String getColumnName(int col) {
		return Field.at(col).name;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return Field.at(col).class_;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return Field.at(col).editable && row < ROW_PACMAN;
	}
}