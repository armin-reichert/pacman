/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.view.dashboard.states;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import javax.swing.table.AbstractTableModel;

import de.amr.games.pacman.controller.bonus.BonusFoodState;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;

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

	private GameController gameController;
	private TiledWorld world;
	private GameStateRecord[] records;
	private boolean dummy;

	public GameStateTableModel() {
		createEmptyRecords();
		dummy = true;
	}

	public GameStateTableModel(GameController gameController) {
		this.gameController = gameController;
		world = gameController.world;
		addTableModelListener(change -> {
			if (change.getColumn() == ColumnInfo.ON_STAGE.ordinal()) {
				handleOnStageStatusChange(change.getFirstRow());
			}
		});
		createEmptyRecords();
		update();
		dummy = false;
	}

	public boolean isDummy() {
		return dummy;
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

	public void update() {
		if (GameController.isGameStarted()) {
			GhostCommand ghostCommand = gameController.ghostCommand;
			Folks folks = gameController.folks;
			fillGhostRecord(records[ROW_BLINKY], ghostCommand, folks.blinky, folks.pacMan);
			fillGhostRecord(records[ROW_PINKY], ghostCommand, folks.pinky, folks.pacMan);
			fillGhostRecord(records[ROW_INKY], ghostCommand, folks.inky, folks.pacMan);
			fillGhostRecord(records[ROW_CLYDE], ghostCommand, folks.clyde, folks.pacMan);
			fillPacManRecord(records[ROW_PACMAN], folks.pacMan);
			fillBonusRecord(records[ROW_BONUS], gameController, world);
			fireTableDataChanged();
		}
	}

	void fillPacManRecord(GameStateRecord r, PacMan pacMan) {
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

	void fillGhostRecord(GameStateRecord r, GhostCommand ghostCommand, Ghost ghost, PacMan pacMan) {
		r.creature = ghost;
		r.included = world.contains(ghost);
		r.name = ghost.name;
		r.tile = ghost.tile();
		r.target = ghost.getSteering().targetTile().orElse(null);
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

	void fillBonusRecord(GameStateRecord r, GameController gameController, TiledWorld world) {
		r.included = false;
		r.name = "Bonus";
		r.tile = null;
		r.state = BonusFoodState.BONUS_INACTIVE.name();
		r.ticksRemaining = r.duration = 0;
		world.temporaryFood().filter(ArcadeBonus.class::isInstance).map(ArcadeBonus.class::cast).ifPresent(bonus -> {
			r.included = true;
			r.name = bonus.symbol.name();
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
		case ON_STAGE:
			return r.included;
		case NAME:
			return r.name;
		case MOVE_DIR:
			return r.moveDir;
		case WISH_DIR:
			return r.wishDir;
		case TILE:
			return r.tile;
		case TARGET:
			return r.target;
		case SPEED:
			return r.speed;
		case STATE:
			return r.state;
		case REMAINING:
			return r.ticksRemaining;
		case DURATION:
			return r.duration;
		case SANITY:
			return r.ghostSanity;
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (ColumnInfo.at(col) == ColumnInfo.ON_STAGE) {
			records[row].included = (boolean) value;
			fireTableCellUpdated(row, col);
		}
	}

	public GameStateRecord getRecordAt(int row) {
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
		return ColumnInfo.at(col).columnName;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return ColumnInfo.at(col).columnClass;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return ColumnInfo.at(col).editable;
	}
}