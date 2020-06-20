package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.BonusState.INACTIVE;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Game.sec;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
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

	public enum Row {
		Blinky, Pinky, Inky, Clyde, PacMan, Bonus
	}

	static class Data {
		boolean onStage;
		String name;
		Tile tile;
		Tile target;
		Direction moveDir;
		Direction wishDir;
		String state;
		int ticksRemaining;
		int duration;
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

		public Data(Game game, Bonus bonus) {
			onStage = bonus.visible;
			name = bonus.symbol != null ? bonus.toString() : "Bonus";
			state = bonus.getState().name();
			tile = game.maze.bonusSeat.tile;
		}
	}

	static GameStateViewModel SAMPLE_DATA = new GameStateViewModel() {

		//@formatter:off
		Object[][] data = {
		{ true,  "Blinky",  Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, LOCKED,    0, 0, false },
		{ false, "Pinky",   Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, LOCKED,    0, 0, false },
		{ true,  "Inky",    Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, LOCKED,    0, 0, false },
		{ true,  "Clyde",   Tile.at(0, 0), Tile.at(0, 0), DOWN, LEFT, LOCKED,    0, 0, false },
		{ true,  "Pac-Man", Tile.at(0, 0), null,          DOWN, LEFT, SLEEPING,  0, 0, false },
		{ true,  "Bonus",   Tile.at(0, 0), null,          null, null, INACTIVE,  0, 0, false },
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
	public Data[] data = new Data[Row.values().length];

	private GameStateViewModel() {
	}

	public GameStateViewModel(GameController gameController) {
		this.gameController = gameController;
		game = gameController.game;
		ghostCommand = gameController.ghostCommand;
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
		return Column.at(col).name();
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
		if (column == Column.OnStage && row < Row.PacMan.ordinal()) {
			boolean onStage = (boolean) value;
			//@formatter:off
			Ghost ghost = 
					row == Row.Blinky.ordinal() ? game.blinky
				: row == Row.Pinky.ordinal() ? game.pinky 
				: row == Row.Inky.ordinal() ? game.inky 
				: game.clyde;
			//@formatter:on
			if (onStage) {
				game.putOnStage(ghost);
			} else {
				game.pullFromStage(ghost);
			}
			data[row].onStage = (boolean) value;
			fireTableDataChanged();
		}
	}

	public void update() {
		data[Row.Blinky.ordinal()] = new Data(game, ghostCommand, game.blinky);
		data[Row.Pinky.ordinal()] = new Data(game, ghostCommand, game.pinky);
		data[Row.Inky.ordinal()] = new Data(game, ghostCommand, game.inky);
		data[Row.Clyde.ordinal()] = new Data(game, ghostCommand, game.clyde);
		data[Row.PacMan.ordinal()] = new Data(game, game.pacMan);
		data[Row.Bonus.ordinal()] = new Data(game, game.bonus);
		fireTableDataChanged();
	}
}