package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
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
import de.amr.games.pacman.actor.core.Actor;
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

	private final List<Ghost> seats;
	private final List<Direction> eyes;
	private final Set<Actor<?>> actorsOnStage = new HashSet<>();
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

		seats = Arrays.asList(blinky, inky, pinky, clyde);
		eyes = Arrays.asList(LEFT, UP, DOWN, UP);

		dressActors();
		actors().forEach(actor -> actor.setVisible(false));

		pacMan.steering(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setTeleportingDuration(sec(0.5f));

		blinky.during(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.during(ENTERING_HOUSE, blinky.isTakingSeat(seatPosition(2)));
		blinky.during(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.during(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.during(SCATTERING, blinky.isHeadingFor(game().maze().horizonNE));
		blinky.during(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.during(DEAD, blinky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		blinky.setTeleportingDuration(sec(0.5f));

		inky.during(LOCKED, inky.isJumpingUpAndDown(seatPosition(1)));
		inky.during(ENTERING_HOUSE, inky.isTakingSeat(seatPosition(1)));
		inky.during(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.during(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.during(SCATTERING, inky.isHeadingFor(game().maze().horizonSE));
		inky.during(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return game().maze().tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.during(DEAD, inky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		inky.setTeleportingDuration(sec(0.5f));

		pinky.during(LOCKED, pinky.isJumpingUpAndDown(seatPosition(2)));
		pinky.during(ENTERING_HOUSE, pinky.isTakingSeat(seatPosition(2)));
		pinky.during(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.during(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.during(SCATTERING, pinky.isHeadingFor(game().maze().horizonNW));
		pinky.during(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.during(DEAD, pinky.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		pinky.setTeleportingDuration(sec(0.5f));

		clyde.during(LOCKED, clyde.isJumpingUpAndDown(seatPosition(3)));
		clyde.during(ENTERING_HOUSE, clyde.isTakingSeat(seatPosition(3)));
		clyde.during(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.during(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.during(SCATTERING, clyde.isHeadingFor(game().maze().horizonSW));
		clyde.during(CHASING, clyde.isHeadingFor(
				() -> Tile.distanceSq(clyde.tile(), pacMan.tile()) > 8 * 8 ? pacMan.tile() : game().maze().horizonSW));
		clyde.during(DEAD, clyde.isHeadingFor(() -> game().maze().ghostHouseSeats[0]));
		clyde.setTeleportingDuration(sec(0.5f));

		bonus = new Bonus(this);
		bonus.tf.setPosition(game().maze().bonusTile.centerX(), game().maze().bonusTile.y());
		bonus.init();
	}

	public Game game() {
		return game;
	}

	public Theme theme() {
		return theme;
	}

	public int seat(Ghost ghost) {
		return seats.indexOf(ghost);
	}

	public Direction eyes(Ghost ghost) {
		return eyes.get(seat(ghost));
	}

	public Vector2f seatPosition(int seat) {
		Tile seatTile = game.maze().ghostHouseSeats[seat];
		return Vector2f.of(seatTile.centerX(), seatTile.y());
	}

	public void placeOnSeat(Ghost ghost) {
		ghost.tf.setPosition(seatPosition(seat(ghost)));
		ghost.setMoveDir(eyes(ghost));
		ghost.setWishDir(eyes(ghost));
		ghost.enteredNewTile();
	}

	public void setTheme(Theme newTheme) {
		Theme oldTheme = this.theme;
		if (newTheme != oldTheme) {
			this.theme = newTheme;
			dressActors();
			changes.firePropertyChange("theme", oldTheme, newTheme);
		}
	}

	public void setDemoMode(boolean on) {
		if (on) {
			settings.pacManImmortable = true;
			pacMan.steering(pacMan.isMovingRandomlyWithoutTurningBack());
		} else {
			settings.pacManImmortable = false;
			pacMan.steering(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		}
		LOGGER.info("Demo mode = " + on);
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
		actor.setVisible(true);
		actorsOnStage.add(actor);
	}

	public void setActorOffStage(Actor<?> actor) {
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