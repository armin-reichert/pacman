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

	public final static GameStateTableModel LOREM_IPSUM = new GameStateTableModel();

	static {
		LOREM_IPSUM.record(ActorRow.Blinky).name = "Blinky";
		LOREM_IPSUM.record(ActorRow.Pinky).name = "Pinky";
		LOREM_IPSUM.record(ActorRow.Inky).name = "Inky";
		LOREM_IPSUM.record(ActorRow.Clyde).name = "Clyde";
		LOREM_IPSUM.record(ActorRow.PacMan).name = "Pac-Man";
		LOREM_IPSUM.record(ActorRow.Bonus).name = "Bonus";
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

	public enum ActorRow {
		Blinky, Pinky, Inky, Clyde, PacMan, Bonus
	}

	public GameController gameController;
	public Ghost[] ghostByRow;
	private final GameStateRecord[] records;

	public GameStateTableModel() {
		records = new GameStateRecord[ActorRow.values().length];
		for (int i = 0; i < records.length; ++i) {
			records[i] = new GameStateRecord();
		}
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		records = new GameStateRecord[ActorRow.values().length];
		Game game = gameController.game;
		ghostByRow = new Ghost[] { game.blinky, game.pinky, game.inky, game.clyde };
		addTableModelListener(e -> {
			if (e.getColumn() == Field.OnStage.ordinal()) {
				int row = e.getFirstRow();
				if (row != ActorRow.PacMan.ordinal() && row != ActorRow.Bonus.ordinal()) {
					gameController.game.takePart(ghostByRow[row], records[row].takesPart);
				}
			}
		});
	}

	public void update() {
		Game game = gameController.game;
		GhostCommand ghostCommand = gameController.ghostCommand;
		records[ActorRow.Blinky.ordinal()] = new GameStateRecord(game, ghostCommand, game.blinky);
		records[ActorRow.Pinky.ordinal()] = new GameStateRecord(game, ghostCommand, game.pinky);
		records[ActorRow.Inky.ordinal()] = new GameStateRecord(game, ghostCommand, game.inky);
		records[ActorRow.Clyde.ordinal()] = new GameStateRecord(game, ghostCommand, game.clyde);
		records[ActorRow.PacMan.ordinal()] = new GameStateRecord(game, game.pacMan);
		records[ActorRow.Bonus.ordinal()] = new GameStateRecord(game, game.bonus);
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

	public GameStateRecord record(ActorRow row) {
		return records[row.ordinal()];
	}

	public GameStateRecord record(int row) {
		return records[row];
	}

	@Override
	public int getRowCount() {
		return ActorRow.values().length;
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
		return Field.at(col).editable && row < ActorRow.PacMan.ordinal();
	}
}