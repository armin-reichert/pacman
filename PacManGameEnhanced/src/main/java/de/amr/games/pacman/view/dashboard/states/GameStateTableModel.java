package de.amr.games.pacman.view.dashboard.states;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.bonus.BonusFoodState;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostMentalState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.components.Tile;

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
		Sanity(null, GhostMentalState.class, false),
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
		world = gameController.world;
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
		return gameController != null && gameController.game.level != null;
	}

	public void update() {
		if (hasGame()) {
			Game game = gameController.game;
			GhostCommand ghostCommand = gameController.ghostCommand;
			Folks folks = gameController.folks;
			fillGhostRecord(records[ROW_BLINKY], game, ghostCommand, folks.blinky, folks.pacMan);
			fillGhostRecord(records[ROW_PINKY], game, ghostCommand, folks.pinky, folks.pacMan);
			fillGhostRecord(records[ROW_INKY], game, ghostCommand, folks.inky, folks.pacMan);
			fillGhostRecord(records[ROW_CLYDE], game, ghostCommand, folks.clyde, folks.pacMan);
			fillPacManRecord(records[ROW_PACMAN], game, folks.pacMan);
			fillBonusRecord(records[ROW_BONUS], gameController, world);
			fireTableDataChanged();
		}
	}

	void fillPacManRecord(GameStateRecord r, Game game, PacMan pacMan) {
		r.creature = pacMan;
		r.included = world.contains(pacMan);
		r.name = "Pac-Man";
		r.tile = pacMan.tile();
		r.moveDir = pacMan.moveDir;
		r.wishDir = pacMan.wishDir;
		if (pacMan.ai.getState() != null) {
			r.speed = pacMan.getSpeed() * app().clock().getTargetFramerate();
			r.state = pacMan.ai.getState().name();
			r.ticksRemaining = pacMan.ai.state().getTicksRemaining();
			r.duration = pacMan.ai.state().getDuration();
		}
	}

	void fillGhostRecord(GameStateRecord r, Game game, GhostCommand ghostCommand, Ghost ghost, PacMan pacMan) {
		r.creature = ghost;
		r.included = world.contains(ghost);
		r.name = ghost.name;
		r.tile = ghost.tile();
		r.target = ghost.steering().targetTile().orElse(null);
		r.moveDir = ghost.moveDir;
		r.wishDir = ghost.wishDir;
		if (ghost.ai.getState() != null) {
			r.speed = ghost.getSpeed() * app().clock().getTargetFramerate();
			r.state = ghost.ai.getState().name();
			r.ticksRemaining = ghost.ai.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
					: ghost.ai.state().getTicksRemaining();
			r.duration = ghost.ai.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration()
					: ghost.ai.state().getDuration();
		}
		r.ghostSanity = ghost.getMentalState();
		r.pacManCollision = ghost.tile().equals(pacMan.tile());
	}

	void fillBonusRecord(GameStateRecord r, GameController gameController, World world) {
		r.included = false;
		r.name = "Bonus";
		r.tile = null;
		r.state = BonusFoodState.BONUS_INACTIVE.name();
		r.ticksRemaining = r.duration = 0;
		world.temporaryFood().filter(bonus -> bonus instanceof ArcadeBonus).map(ArcadeBonus.class::cast)
				.ifPresent(bonus -> {
					r.included = true;
					r.name = bonus.name();
					r.state = "";
					if (bonus.isConsumed()) {
						r.state = "Consumed";
					} else if (bonus.isActive()) {
						r.state = "Present";
					} else {
						r.state = "Absent";
					}
					r.ticksRemaining = gameController.bonusController.state().getTicksRemaining();
					r.duration = gameController.bonusController.state().getDuration();
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