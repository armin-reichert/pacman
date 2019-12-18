package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
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
	public PacManTheme theme;
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;
	private Bonus bonus;
	private final Set<MazeResident> actorsOnStage = new HashSet<>();

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
		pacMan.setTeleportingDuration(sec(0.25f));

		blinky.initialDir = LEFT;
		blinky.ghostHousePlace = 0;
		blinky.setSteering(SCATTERING, headingForTargetTile(() -> maze.scatterTileNE));
		blinky.setSteering(CHASING, headingForTargetTile(pacMan::tile));

		inky.initialDir = UP;
		inky.ghostHousePlace = 1;
		inky.setSteering(SCATTERING, headingForTargetTile(() -> maze.scatterTileSE));
		inky.setSteering(CHASING, headingForTargetTile(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		}));

		pinky.initialDir = DOWN;
		pinky.ghostHousePlace = 2;
		pinky.setSteering(SCATTERING, headingForTargetTile(() -> maze.scatterTileNW));
		pinky.setSteering(CHASING, headingForTargetTile(() -> pacMan.tilesAhead(4)));

		clyde.initialDir = UP;
		clyde.ghostHousePlace = 3;
		clyde.setSteering(SCATTERING, headingForTargetTile(() -> maze.scatterTileSW));
		clyde.setSteering(CHASING,
				headingForTargetTile(() -> clyde.distanceSq(pacMan) > 8 * 8 ? pacMan.tile() : maze.scatterTileSW));

		ghosts().forEach(ghost -> {
			ghost.setTeleportingDuration(sec(0.5f));
			ghost.setSteering(GhostState.LEAVING_HOUSE, leavingGhostHouse(maze));
			ghost.setSteering(GhostState.FRIGHTENED, movingRandomlyNoReversing());
			if (ghost != blinky) {
				ghost.setSteering(GhostState.LOCKED, jumpingUpAndDown(maze, ghost.ghostHousePlace));
				ghost.setSteering(GhostState.ENTERING_HOUSE, enteringGhostHouse(maze, ghost, ghost.ghostHousePlace));
			} else {
				ghost.setSteering(GhostState.ENTERING_HOUSE, enteringGhostHouse(maze, ghost, 2));
			}
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

	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(this::onStage);
	}

	public Stream<MazeResident> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<MazeResident> actorsOnStage() {
		return actors().filter(this::onStage);
	}

	public void putOnStage(MazeResident actor) {
		actor.init();
		actor.show();
		actorsOnStage.add(actor);
	}

	public void removeFromStage(MazeResident actor) {
		actor.hide();
		actorsOnStage.remove(actor);
	}

	public boolean onStage(MazeResident actor) {
		return actorsOnStage.contains(actor);
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
}