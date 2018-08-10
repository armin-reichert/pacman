package de.amr.games.pacman.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static java.lang.Math.round;

import java.util.Map;
import java.util.function.Function;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.routing.impl.NavigationSystem;
import de.amr.games.pacman.view.PacManGameUI;
import de.amr.statemachine.StateMachine;

/**
 * @param <S> maze mover state type
 */
public abstract class MazeMover<S> extends MazeEntity {

	private static final int TELEPORT_TILES = 6;

	public final Maze maze;
	public final Tile homeTile;
	private final Map<S, Navigation> navigation;
	private Function<MazeMover<S>, Float> fnSpeed;
	private int dir;
	private int nextDir;

	protected MazeMover(Maze maze, Tile homeTile, Map<S, Navigation> navigation) {
		this.maze = maze;
		this.homeTile = homeTile;
		this.navigation = navigation;
		this.fnSpeed = mover -> 0f;
	}

	// State machine

	protected abstract StateMachine<S, GameEvent> getStateMachine();

	public S getState() {
		return getStateMachine().currentState();
	}

	@Override
	public void init() {
		getStateMachine().init();
	}

	@Override
	public void update() {
		getStateMachine().update();
	}

	public void processEvent(GameEvent e) {
		getStateMachine().enqueue(e);
		getStateMachine().update();
	}

	// Movement

	public void setNavigation(S state, Navigation navigation) {
		this.navigation.put(state, navigation);
	}

	public Navigation getNavigation() {
		return navigation.getOrDefault(getState(), NavigationSystem.forward());
	}

	public float getSpeed() {
		return fnSpeed.apply(this);
	}

	public void setSpeed(Function<MazeMover<S>, Float> fnSpeed) {
		this.fnSpeed = fnSpeed;
	}

	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int dir) {
		this.nextDir = dir;
	}

	public boolean isOutsideMaze() {
		Tile tile = getTile();
		return tile.row < 0 || tile.row >= maze.numRows() || tile.col < 0 || tile.col >= maze.numCols();
	}

	public void move() {
		if (isOutsideMaze()) {
			teleport();
			return;
		}
		nextDir = getNavigation().computeRoute(this).getDirection();
		if (canMove(nextDir)) {
			dir = nextDir;
		}
		if (canMove(dir)) {
			tf.moveTo(computePosition(dir));
		} else {
			placeAt(getTile());
		}
	}
	
	private void teleport() {
		Tile tile = getTile();
		if (tile.col > (maze.numCols() - 1) + TELEPORT_TILES) {
			// return to the maze from the left
			placeAt(0, tile.row);
		} else if (tile.col < -TELEPORT_TILES) {
			// return to the maze from the right
			placeAt(maze.numCols() - 1, tile.row);
		} else {
			tf.moveTo(computePosition(dir));
		}
	}

	public boolean canMove(int goal) {
		if (isOutsideMaze()) {
			return true;
		}
		Tile current = getTile();
		if (goal == Top4.W && current.col <= 0) {
			return true; // enter teleport space on the left
		}
		if (goal == Top4.E && current.col >= maze.numCols() - 1) {
			return true; // enter teleport space on the right
		}
		Tile next = computeNextTile(current, goal);
		if (next.equals(current)) {
			return true; // move doesn't leave current tile
		}
		if (maze.getContent(next) == WALL) {
			return false;
		}
		if (goal == TOPOLOGY.right(dir) || goal == TOPOLOGY.left(dir)) {
			placeAt(getTile()); // TODO this is not 100% correct
			return isExactlyOverTile();
		}
		return true;
	}

	public Tile computeNextTile(Tile current, int dir) {
		Vector2f nextPosition = computePosition(dir);
		float x = nextPosition.x, y = nextPosition.y;
		switch (dir) {
		case Top4.W:
			return new Tile(round(x) / PacManGameUI.TS, current.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / PacManGameUI.TS, current.row);
		case Top4.N:
			return new Tile(current.col, round(y) / PacManGameUI.TS);
		case Top4.S:
			return new Tile(current.col, round(y + getHeight()) / PacManGameUI.TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private Vector2f computePosition(int dir) {
		Vector2f v_dir = Vector2f.of(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
		return sum(tf.getPosition(), smul(getSpeed(), v_dir));
	}
}