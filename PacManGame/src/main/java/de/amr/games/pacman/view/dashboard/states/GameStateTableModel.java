package de.amr.games.pacman.view.dashboard.states;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.model.game.Game.sec;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostSanity;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bonus;
import de.amr.games.pacman.model.world.components.BonusState;

/**
 * Data model of the table displaying actor data.
 * 
 * @author Armin Reichert
 */
class GameStateTableModel extends AbstractTableModel {

	public static final int ROW_BLINKY = 0;
	public static final int ROW_PINKY = 1;
	public static final int ROW_INKY = 2;
	public static final int ROW_CLYDE = 3;
	public static final int ROW_PACMAN = 4;
	public static final int ROW_BONUS = 5;

	public static final int NUM_ROWS = 6;

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
		Sanity(null, GhostSanity.class, false),
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
	private World world;
	private GameStateRecord[] records;

	public GameStateTableModel() {
		createEmptyRecords();
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		world = gameController.world();
		addTableModelListener(change -> {
			if (change.getColumn() == ColumnInfo.OnStage.ordinal()) {
				handleOnStageStatusChange(change.getFirstRow());
			}
		});
		createEmptyRecords();
		update();
	}

	private void createEmptyRecords() {
		records = new GameStateRecord[NUM_ROWS];
		for (int i = 0; i < records.length; ++i) {
			records[i] = new GameStateRecord();
		}
		// for window builder
		records[ROW_BLINKY].name = "Blinky";
		records[ROW_PINKY].name = "Pinky";
		records[ROW_INKY].name = "Inky";
		records[ROW_CLYDE].name = "Clyde";
		records[ROW_PACMAN].name = "Pac-Man";
		records[ROW_BONUS].name = "Bonus";
	}

	private void handleOnStageStatusChange(int row) {
		GameStateRecord r = records[row];
		if (r.creature instanceof Ghost) {
			if (r.included) {
				world.include(r.creature);
			} else {
				world.exclude(r.creature);
			}
		}
	}

	public GameController getGameController() {
		return gameController;
	}

	public boolean hasGame() {
		return gameController != null && gameController.game().isPresent();
	}

	public void update() {
		if (gameController != null) {
			gameController.game().ifPresent(game -> {
				gameController.ghostCommand().ifPresent(ghostCommand -> {
					Folks folks = gameController.folks();
					fillGhostRecord(records[ROW_BLINKY], game, ghostCommand, folks.blinky, folks.pacMan);
					fillGhostRecord(records[ROW_PINKY], game, ghostCommand, folks.pinky, folks.pacMan);
					fillGhostRecord(records[ROW_INKY], game, ghostCommand, folks.inky, folks.pacMan);
					fillGhostRecord(records[ROW_CLYDE], game, ghostCommand, folks.clyde, folks.pacMan);
					fillPacManRecord(records[ROW_PACMAN], game, folks.pacMan);
					fillBonusRecord(records[ROW_BONUS], gameController, world);
					fireTableDataChanged();
				});
			});
		}
	}

	void fillPacManRecord(GameStateRecord r, Game game, PacMan pacMan) {
		r.creature = pacMan;
		r.included = world.contains(pacMan);
		r.name = "Pac-Man";
		r.tile = pacMan.tileLocation();
		r.moveDir = pacMan.moveDir();
		r.wishDir = pacMan.wishDir();
		if (pacMan.getState() != null) {
			r.speed = pacMan.speed() * app().clock().getTargetFramerate();
			r.state = pacMan.getPower() == 0 ? pacMan.getState().name() : "POWER";
			r.ticksRemaining = pacMan.getPower() == 0 ? pacMan.state().getTicksRemaining() : pacMan.getPower();
			r.duration = pacMan.getPower() == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
		}
	}

	void fillGhostRecord(GameStateRecord r, Game game, GhostCommand ghostCommand, Ghost ghost, PacMan pacMan) {
		r.creature = ghost;
		r.included = world.contains(ghost);
		r.name = ghost.name;
		r.tile = ghost.tileLocation();
		r.target = ghost.targetTile().orElse(null);
		r.moveDir = ghost.moveDir();
		r.wishDir = ghost.wishDir();
		if (ghost.getState() != null) {
			r.speed = ghost.speed() * app().clock().getTargetFramerate();
			r.state = ghost.getState().name();
			r.ticksRemaining = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
					: ghost.state().getTicksRemaining();
			r.duration = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration() : ghost.state().getDuration();
		}
		r.ghostSanity = ghost.getSanity();
		r.pacManCollision = ghost.tileLocation().equals(pacMan.tileLocation());
	}

	void fillBonusRecord(GameStateRecord r, GameController gameController, World world) {
		gameController.bonusControl().ifPresent(bonusControl -> {
			Bonus bonus = world.getBonus().orElse(null);
			r.included = bonus != null && bonus.state != BonusState.INACTIVE;
			r.name = bonus != null ? bonus.symbol : "Bonus";
			r.tile = bonus != null ? bonus.location : null;
			r.state = bonusControl.getState().name();
			r.ticksRemaining = bonusControl.state().getTicksRemaining();
			r.duration = bonusControl.state().getDuration();
		});
	}

	@Override
	public Object getValueAt(int row, int col) {
		GameStateRecord r = records[row];
		switch (ColumnInfo.at(col)) {
		case OnStage:
			return r.included;
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
			records[row].included = (boolean) value;
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