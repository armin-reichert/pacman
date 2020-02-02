package de.amr.games.pacman.actor;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Timing.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.core.MovingActor;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
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
	public final Bonus bonus;

	private final List<Ghost> seatAssignment;
	private final List<Direction> seatGhostDirections;
	private final Set<MovingActor<?>> actorsOnStage = new HashSet<>();
	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private final Game game;
	private Theme theme;

	public Cast(Game game, Theme theme) {
		this.game = game;
		this.theme = theme;

		pacMan = new PacMan(this);
		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");

		seatAssignment = Arrays.asList(blinky, inky, pinky, clyde);
		seatGhostDirections = Arrays.asList(LEFT, UP, DOWN, UP);

		dressActors();
		actors().forEach(actor -> actor.setVisible(false));

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setTeleportingDuration(sec(0.5f));

		blinky.behavior(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(seatPosition(2)));
		blinky.behavior(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.behavior(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.behavior(SCATTERING, blinky.isHeadingFor(game().maze().horizonNE));
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.behavior(DEAD, blinky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		blinky.setTeleportingDuration(sec(0.5f));

		inky.behavior(LOCKED, inky.isJumpingUpAndDown(seatPosition(1)));
		inky.behavior(ENTERING_HOUSE, inky.isTakingSeat(seatPosition(1)));
		inky.behavior(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.behavior(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.behavior(SCATTERING, inky.isHeadingFor(game().maze().horizonSE));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return game().maze().tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.behavior(DEAD, inky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		inky.setTeleportingDuration(sec(0.5f));

		pinky.behavior(LOCKED, pinky.isJumpingUpAndDown(seatPosition(2)));
		pinky.behavior(ENTERING_HOUSE, pinky.isTakingSeat(seatPosition(2)));
		pinky.behavior(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.behavior(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.behavior(SCATTERING, pinky.isHeadingFor(game().maze().horizonNW));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.behavior(DEAD, pinky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		pinky.setTeleportingDuration(sec(0.5f));

		clyde.behavior(LOCKED, clyde.isJumpingUpAndDown(seatPosition(3)));
		clyde.behavior(ENTERING_HOUSE, clyde.isTakingSeat(seatPosition(3)));
		clyde.behavior(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.behavior(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.behavior(SCATTERING, clyde.isHeadingFor(game().maze().horizonSW));
		clyde.behavior(CHASING, clyde.isHeadingFor(
				() -> Tile.distanceSq(clyde.tile(), pacMan.tile()) > 8 * 8 ? pacMan.tile() : game().maze().horizonSW));
		clyde.behavior(DEAD, clyde.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		clyde.setTeleportingDuration(sec(0.5f));

		bonus = new Bonus(this);
		bonus.init();
	}

	public Game game() {
		return game;
	}

	public Theme theme() {
		return theme;
	}

	public int seat(Ghost ghost) {
		return seatAssignment.indexOf(ghost);
	}

	public Direction seatGhostDirection(Ghost ghost) {
		return seatGhostDirections.get(seat(ghost));
	}

	public Vector2f seatPosition(int seat) {
		Tile seatTile = game.maze().ghostHouseSeats[seat];
		return Vector2f.of(seatTile.centerX(), seatTile.y());
	}

	public void placeOnSeat(Ghost ghost) {
		ghost.tf.setPosition(seatPosition(seat(ghost)));
		ghost.setMoveDir(seatGhostDirection(ghost));
		ghost.setWishDir(seatGhostDirection(ghost));
		ghost.enteredNewTile();
	}

	public void setDemoMode(boolean on) {
		if (on) {
			settings.pacManImmortable = true;
			pacMan.behavior(pacMan.isMovingRandomlyWithoutTurningBack());
		} else {
			settings.pacManImmortable = false;
			pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
	}

	private void dressActors() {
		pacMan.dress();
		blinky.dress(GhostColor.RED);
		pinky.dress(GhostColor.PINK);
		inky.dress(GhostColor.CYAN);
		clyde.dress(GhostColor.ORANGE);
	}

	public void addThemeListener(PropertyChangeListener subscriber) {
		changes.addPropertyChangeListener("theme", subscriber);
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(this::onStage);
	}

	public Stream<MovingActor<?>> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<MovingActor<?>> actorsOnStage() {
		return actors().filter(this::onStage);
	}

	public boolean onStage(MovingActor<?> actor) {
		return actorsOnStage.contains(actor);
	}

	public void putActorOnStage(MovingActor<?> actor) {
		actor.init();
		actor.setVisible(true);
		actorsOnStage.add(actor);
	}

	public void pullActorFromStage(MovingActor<?> actor) {
		actor.setVisible(false);
		actorsOnStage.remove(actor);
	}

	public void showBonus() {
		bonus.setSymbol(game().level().bonusSymbol);
		bonus.setValue(game().level().bonusValue);
		bonus.activate();
	}

	public void hideBonus() {
		bonus.init();
	}
}