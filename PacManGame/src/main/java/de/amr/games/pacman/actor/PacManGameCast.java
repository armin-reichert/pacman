package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.behavior.common.Steerings.jumpingUpAndDown;
import static de.amr.games.pacman.actor.behavior.common.Steerings.movingRandomlyNoReversing;
import static de.amr.games.pacman.actor.behavior.common.Steerings.steeredByKeys;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.sec;
import static de.amr.graph.grid.impl.Grid4Topology.N;
import static de.amr.graph.grid.impl.Grid4Topology.S;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.stream.Stream;

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
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;
	public PacManTheme theme;
	private Bonus bonus;

	public PacManGameCast(PacManGame game, PacManTheme theme) {
		this.game = game;

		pacMan = new PacMan(this);
		blinky = new Ghost("Blinky", this);
		pinky = new Ghost("Pinky", this);
		inky = new Ghost("Inky", this);
		clyde = new Ghost("Clyde", this);

		setTheme(theme);

		// configure the actors

		pacMan.steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);
		pacMan.teleportingTicks = sec(0.5f);

		blinky.initialDir = W;
		blinky.initialTile = game.maze.ghostHome[0];
		blinky.scatterTile = game.maze.scatterTileNE;
		blinky.revivalTile = game.maze.ghostHome[2];
		blinky.teleportingTicks = sec(1);
		blinky.fnChasingTarget = pacMan::tile;

		pinky.initialDir = S;
		pinky.initialTile = game.maze.ghostHome[2];
		pinky.scatterTile = game.maze.scatterTileNW;
		pinky.revivalTile = game.maze.ghostHome[2];
		pinky.teleportingTicks = sec(1);
		pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);

		inky.initialDir = N;
		inky.initialTile = game.maze.ghostHome[1];
		inky.scatterTile = game.maze.scatterTileSE;
		inky.revivalTile = game.maze.ghostHome[1];
		inky.teleportingTicks = sec(1);
		inky.fnChasingTarget = () -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return game.maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		};

		clyde.initialDir = N;
		clyde.initialTile = game.maze.ghostHome[3];
		clyde.scatterTile = game.maze.scatterTileSW;
		clyde.revivalTile = game.maze.ghostHome[3];
		clyde.teleportingTicks = sec(1);
		clyde.fnChasingTarget = () -> clyde.tileDistanceSq(pacMan) > 8 * 8 ? pacMan.tile()
				: game.maze.scatterTileSW;

		ghosts().forEach(ghost -> ghost.setSteering(GhostState.FRIGHTENED, movingRandomlyNoReversing()));

		// Blinky does not jump when locked
		Stream.of(pinky, inky, clyde).forEach(ghost -> ghost.setSteering(GhostState.LOCKED, jumpingUpAndDown()));
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
		NESW.dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir)));
		pacMan.sprites.set("dying", theme.spr_pacManDying());
		pacMan.sprites.set("full", theme.spr_pacManFull());
		pacMan.sprites.select("full");
	}

	private void setGhostSprites(Ghost ghost, GhostColor color) {
		NESW.dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
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
		return ghosts().filter(Ghost::isActive);
	}

	public Stream<Actor<?>> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<Actor<?>> activeActors() {
		return actors().filter(Actor::isActive);
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

	public void chasingSoundOn() {
		if (!theme.snd_ghost_chase().isRunning()) {
			theme.snd_ghost_chase().loop();
		}
	}

	public void chasingSoundOff(Ghost caller) {
		// if caller is the only chasing ghost, turn it off
		if (activeGhosts().filter(ghost -> caller != ghost).noneMatch(ghost -> ghost.getState() == CHASING)) {
			theme.snd_ghost_chase().stop();
		}
	}

	public void deadSoundOn() {
		if (!theme.snd_ghost_dead().isRunning()) {
			theme.snd_ghost_dead().loop();
		}
	}

	public void deadSoundOff(Ghost caller) {
		// if caller is the only dead ghost, turn it off
		if (activeGhosts().filter(ghost -> ghost != caller).noneMatch(ghost -> ghost.getState() == DEAD)) {
			theme.snd_ghost_dead().stop();
		}
	}

	// rules for leaving the ghost house

	/**
	 * The first control used to evaluate when the ghosts leave home is a personal counter each ghost
	 * retains for tracking the number of dots Pac-Man eats. Each ghost's "dot counter" is reset to zero
	 * when a level begins and can only be active when inside the ghost house, but only one ghost's
	 * counter can be active at any given time regardless of how many ghosts are inside.
	 * 
	 * <p>
	 * The order of preference for choosing which ghost's counter to activate is: Pinky, then Inky, and
	 * then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its dot
	 * counter increased by one. Each ghost also has a "dot limit" associated with his counter, per
	 * level.
	 * 
	 * <p>
	 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its
	 * dot counter is deactivated (but not reset). The most-preferred ghost still waiting inside the
	 * house (if any) activates its timer at this point and begins counting dots.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeaveHouse(Ghost ghost) {
		if (ghost == blinky) {
			return true;
		}
		Ghost next = Stream.of(pinky, inky, clyde).filter(g -> g.getState() == GhostState.LOCKED).findFirst()
				.orElse(null);
		if (ghost != next) {
			return false;
		}
		if (ghost.foodCount >= getFoodLimit(ghost)) {
			return true;
		}
		if (game.globalFoodCounterEnabled && game.globalFoodCount >= getGlobalFoodCounterLimit(ghost)) {
			return true;
		}
		int timeout = game.level.number < 5 ? sec(4) : sec(3);
		if (pacMan.ticksSinceLastMeal > timeout) {
			LOGGER.info(() -> String.format("Releasing ghost %s (Pac-Man eat timer expired)", ghost.name()));
			return true;
		}
		return false;
	}

	public void updateFoodCounter() {
		if (game.globalFoodCounterEnabled) {
			game.globalFoodCount++;
			LOGGER.fine(() -> String.format("Global Food Counter=%d", game.globalFoodCount));
			if (game.globalFoodCount == 32 && clyde.getState() == GhostState.LOCKED) {
				game.globalFoodCounterEnabled = false;
				game.globalFoodCount = 0;
			}
			return;
		}
		/*@formatter:off*/
		Stream.of(pinky, inky, clyde)
			.filter(ghost -> ghost.getState() == GhostState.LOCKED)
			.findFirst()
			.ifPresent(preferredGhost -> {
				preferredGhost.foodCount += 1;
				LOGGER.fine(() -> String.format("Food Counter[%s]=%d", preferredGhost.name(), preferredGhost.foodCount));
		});
		/*@formatter:on*/
	}

	/**
	 * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level
	 * begins. For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This
	 * results in Pinky exiting immediately which, in turn, activates Inky's dot counter. His counter
	 * must then reach or exceed 30 dots before he can leave the house.
	 * 
	 * <p>
	 * Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and starts
	 * counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's
	 * dot limit is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the
	 * house as soon as the level begins from now on.
	 * 
	 * <p>
	 * Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
	 * and will leave the ghost house immediately at the start of every level.
	 * 
	 * @param ghost
	 *                a ghost
	 * @return the ghosts's current food limit
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private int getFoodLimit(Ghost ghost) {
		if (ghost == pinky) {
			return 0;
		}
		if (ghost == inky) {
			return game.level.number == 1 ? 30 : 0;
		}
		if (ghost == clyde) {
			return game.level.number == 1 ? 60 : game.level.number == 2 ? 50 : 0;
		}
		return 0;
	}

	private int getGlobalFoodCounterLimit(Ghost ghost) {
		return (ghost == pinky) ? 7 : (ghost == inky) ? 17 : (ghost == clyde) ? 32 : 0;
	}
}