package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.behavior.Steerings.followsKeys;
import static de.amr.games.pacman.actor.behavior.Steerings.isHeadingFor;
import static de.amr.games.pacman.actor.behavior.Steerings.isJumpingUpAndDown;
import static de.amr.games.pacman.actor.behavior.Steerings.isLeavingGhostHouse;
import static de.amr.games.pacman.actor.behavior.Steerings.isMovingRandomlyWithoutTurningBack;
import static de.amr.games.pacman.actor.behavior.Steerings.isTakingSeat;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Direction.dirs;
import static de.amr.games.pacman.model.Timing.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;

/**
 * The cast (set of actors) of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Cast {

	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	private final Game game;
	private Theme theme;
	private Bonus bonus;
	private final Set<Actor<?>> actorsOnStage = new HashSet<>();
	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public Cast(Game game, Theme theme) {
		this.game = game;

		pacMan = new PacMan(this);

		blinky = new Ghost("Blinky", this, 0);
		inky = new Ghost("Inky", this, 1);
		pinky = new Ghost("Pinky", this, 2);
		clyde = new Ghost("Clyde", this, 3);

		// initially, all actors are off-stage
		actors().forEach(actor -> setActorOffStage(actor));

		// configure the actors

		setTheme(theme);

		pacMan.steering(followsKeys(pacMan, VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setTeleportingDuration(sec(0.5f));

		blinky.eyes = LEFT;
		blinky.during(SCATTERING, isHeadingFor(blinky, maze().horizonNE));
		blinky.during(CHASING, isHeadingFor(blinky, pacMan::tile));
		blinky.during(ENTERING_HOUSE, isTakingSeat(blinky, 2));

		inky.eyes = UP;
		inky.during(SCATTERING, isHeadingFor(inky, maze().horizonSE));
		inky.during(CHASING, isHeadingFor(inky, () -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return maze().tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.during(LOCKED, isJumpingUpAndDown(inky));
		inky.during(ENTERING_HOUSE, isTakingSeat(inky));

		pinky.eyes = DOWN;
		pinky.during(SCATTERING, isHeadingFor(pinky, maze().horizonNW));
		pinky.during(CHASING, isHeadingFor(pinky, () -> pacMan.tilesAhead(4)));
		pinky.during(LOCKED, isJumpingUpAndDown(pinky));
		pinky.during(ENTERING_HOUSE, isTakingSeat(pinky));

		clyde.eyes = UP;
		clyde.during(SCATTERING, isHeadingFor(clyde, maze().horizonSW));
		clyde.during(CHASING, isHeadingFor(clyde,
				() -> Tile.distanceSq(clyde.tile(), pacMan.tile()) > 8 * 8 ? pacMan.tile() : maze().horizonSW));
		clyde.during(LOCKED, isJumpingUpAndDown(clyde));
		clyde.during(ENTERING_HOUSE, isTakingSeat(clyde));

		ghosts().forEach(ghost -> {
			ghost.setTeleportingDuration(sec(0.5f));
			ghost.during(LEAVING_HOUSE, isLeavingGhostHouse(ghost));
			ghost.during(FRIGHTENED, isMovingRandomlyWithoutTurningBack(ghost));
		});
	}

	public Game game() {
		return game;
	}

	public Maze maze() {
		return game.maze();
	}

	public Theme theme() {
		return theme;
	}

	public void setTheme(Theme newTheme) {
		Theme oldTheme = this.theme;
		this.theme = newTheme;
		clotheActors();
		changes.firePropertyChange("theme", oldTheme, newTheme);
	}

	public void addThemeListener(PropertyChangeListener subscriber) {
		changes.addPropertyChangeListener("theme", subscriber);
	}

	private void clotheActors() {
		clothePacMan();
		clotheGhosts(blinky, GhostColor.RED);
		clotheGhosts(pinky, GhostColor.PINK);
		clotheGhosts(inky, GhostColor.CYAN);
		clotheGhosts(clyde, GhostColor.ORANGE);
	}

	private void clothePacMan() {
		dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir.ordinal())));
		pacMan.sprites.set("dying", theme.spr_pacManDying());
		pacMan.sprites.set("full", theme.spr_pacManFull());
		pacMan.sprites.select("full");
	}

	private void clotheGhosts(Ghost ghost, GhostColor color) {
		dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir.ordinal()));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir.ordinal()));
		});
		// sprite keys: "number-200", "number-400", "number-800", "number-1600"
		for (int number : new int[] { 200, 400, 800, 1600 }) {
			ghost.sprites.set("number-" + number, theme.spr_number(number));
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

	public Stream<Actor<?>> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<Actor<?>> actorsOnStage() {
		return actors().filter(this::onStage);
	}

	public boolean onStage(Actor<?> actor) {
		return actorsOnStage.contains(actor);
	}

	public void setActorOnStage(Actor<?> actor) {
		actor.init();
		actor.show();
		actorsOnStage.add(actor);
	}

	public void setActorOffStage(Actor<?> actor) {
		actor.hide();
		actorsOnStage.remove(actor);
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