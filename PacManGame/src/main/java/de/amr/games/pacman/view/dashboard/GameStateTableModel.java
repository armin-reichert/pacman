package de.amr.games.pacman.view.dashboard;

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

	public ActorRecord[] records = new ActorRecord[ActorRow.values().length];

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

	@Override
	public Object getValueAt(int row, int col) {
		ActorRecord r = records[row];
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
}