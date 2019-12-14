package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.behavior.Steerings.enteringGhostHouse;
import static de.amr.games.pacman.actor.behavior.Steerings.headingForTargetTile;
import static de.amr.games.pacman.actor.behavior.Steerings.jumpingUpAndDown;
import static de.amr.games.pacman.actor.behavior.Steerings.leavingGhostHouse;
import static de.amr.games.pacman.actor.behavior.Steerings.movingRandomlyNoReversing;
import static de.amr.games.pacman.actor.behavior.Steerings.steeredByKeys;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Direction.dirs;
import static de.amr.games.pacman.model.Timing.sec;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The cast (set of actors) in the PacMan game.
 * 
 * @author Armin Reichert
 */
public class PacManGameCast {

	public final PacManGame game;
	public final Maze maze;
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;
	public PacManTheme theme;
	private Bonus bonus;
	private final Set<MazeResident> activeActors = new HashSet<>();

	public PacManGameCast(PacManGame game, PacManTheme theme) {
		this.game = game;
		this.maze = game.maze;

		// create the actors

		pacMan = new PacMan(this);
		blinky = new Ghost("Blinky", this);
		pinky = new Ghost("Pinky", this);
		inky = new Ghost("Inky", this);
		clyde = new Ghost("Clyde", this);

		setTheme(theme);

		// configure the actors

		pacMan.steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);
		pacMan.teleportingTicks = sec(0.25f);

		blinky.initialDir = LEFT;
		blinky.initialTile = maze.ghostHome[0];
		blinky.scatterTile = maze.scatterTileNE;
		blinky.teleportingTicks = sec(0.5f);
		blinky.fnChasingTarget = pacMan::tile;

		pinky.initialDir = DOWN;
		pinky.initialTile = maze.ghostHome[2];
		pinky.scatterTile = maze.scatterTileNW;
		pinky.teleportingTicks = sec(0.5f);
		pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);

		inky.initialDir = UP;
		inky.initialTile = maze.ghostHome[1];
		inky.scatterTile = maze.scatterTileSE;
		inky.teleportingTicks = sec(0.5f);
		inky.fnChasingTarget = () -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		};

		clyde.initialDir = UP;
		clyde.initialTile = maze.ghostHome[3];
		clyde.scatterTile = maze.scatterTileSW;
		clyde.teleportingTicks = sec(0.5f);
		clyde.fnChasingTarget = () -> clyde.distanceSq(pacMan) > 8 * 8 ? pacMan.tile() : maze.scatterTileSW;

		ghosts().forEach(ghost -> {
			ghost.setSteering(GhostState.LEAVING_HOUSE, leavingGhostHouse(maze));
			ghost.setSteering(GhostState.FRIGHTENED, movingRandomlyNoReversing());
			ghost.setSteering(GhostState.LOCKED, ghost == blinky ? headingForTargetTile() : jumpingUpAndDown());
			ghost.setSteering(GhostState.ENTERING_HOUSE,
					enteringGhostHouse(maze, ghost == blinky ? maze.ghostHome[2] : ghost.initialTile));
		});
	}

	public void setTheme(PacManTheme theme) {
		this.theme = theme;
		setPacManSprites();
		setGhostSprites(blinky, GhostColor.RED);
		setGhostSprites(pinky, GhostColor.PINK);
		setGhostSprites(inky, GhostColor.CYAN);
		setGhostSprites(clyde, GhostColor.ORANGE);
	}

	private void setPacManSprites() {
		dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir.ordinal())));
		pacMan.sprites.set("dying", theme.spr_pacManDying());
		pacMan.sprites.set("full", theme.spr_pacManFull());
		pacMan.sprites.select("full");
	}

	private void setGhostSprites(Ghost ghost, GhostColor color) {
		dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir.ordinal()));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir.ordinal()));
		});
		for (int i = 0; i < 4; ++i) {
			ghost.sprites.set("value-" + i, theme.spr_greenNumber(i));
		}
		ghost.sprites.set("frightened", theme.spr_ghostFrightened());
		ghost.sprites.set("flashing", theme.spr_ghostFlashing());
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> activeGhosts() {
		return ghosts().filter(this::isActive);
	}

	public Stream<MazeResident> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<MazeResident> activeActors() {
		return actors().filter(this::isActive);
	}

	public void activate(MazeResident actor) {
		actor.init();
		actor.resident().show();
		activeActors.add(actor);
	}

	public void deactivate(MazeResident actor) {
		actor.resident().hide();
		activeActors.remove(actor);
	}

	public boolean isActive(MazeResident actor) {
		return activeActors.contains(actor);
	}

	public Optional<Bonus> bonus() {
		return Optional.ofNullable(bonus);
	}

	public void addBonus() {
		bonus = new Bonus(this);
		bonus.init();
	}

	public void removeBonus() {
		bonus = null;
	}

	public void turnGhostIsChasingSoundOn() {
		if (!theme.snd_ghost_chase().isRunning()) {
			theme.snd_ghost_chase().loop();
		}
	}

	public void turnGhostIsChasingSoundOff(Ghost caller) {
		// if caller is the only chasing ghost, turn sound off
		if (activeGhosts().filter(ghost -> caller != ghost).noneMatch(ghost -> ghost.getState() == CHASING)) {
			theme.snd_ghost_chase().stop();
		}
	}

	public void turnGhostIsDeadSoundOn() {
		if (!theme.snd_ghost_dead().isRunning()) {
			theme.snd_ghost_dead().loop();
		}
	}

	public void turnGhostIsDeadSoundOff(Ghost caller) {
		// if caller is the only dead ghost, turn sound off
		if (activeGhosts().filter(ghost -> ghost != caller).noneMatch(ghost -> ghost.getState() == DEAD)) {
			theme.snd_ghost_dead().stop();
		}
	}
}