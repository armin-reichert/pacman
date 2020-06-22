package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.sec;

import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Holds the actor data displayed in the game state view.
 * 
 * @author Armin Reichert
 */
public class ActorRecord {

	public boolean takesPart;
	public String name;
	public Tile tile;
	public Tile target;
	public Direction moveDir;
	public Direction wishDir;
	public float speed;
	public String state;
	public Sanity ghostSanity;
	public int ticksRemaining;
	public int duration;
	public boolean pacManCollision;

	public ActorRecord(Game game, PacMan pacMan) {
		takesPart = game.takesPart(pacMan);
		name = "Pac-Man";
		tile = pacMan.tile();
		moveDir = pacMan.moveDir();
		wishDir = pacMan.wishDir();
		if (pacMan.getState() != null) {
			speed = pacMan.currentSpeed(game);
			state = pacMan.power == 0 ? pacMan.getState().name() : "POWER";
			ticksRemaining = pacMan.power == 0 ? pacMan.state().getTicksRemaining() : pacMan.power;
			duration = pacMan.power == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
		}
	}

	public ActorRecord(Game game, GhostCommand ghostCommand, Ghost ghost) {
		takesPart = game.takesPart(ghost);
		name = ghost.name;
		tile = ghost.tile();
		target = ghost.targetTile();
		moveDir = ghost.moveDir();
		wishDir = ghost.wishDir();
		if (ghost.getState() != null) {
			speed = ghost.currentSpeed(game);
			state = ghost.getState().name();
			ticksRemaining = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
					: ghost.state().getTicksRemaining();
			duration = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration() : ghost.state().getDuration();
		}
		ghostSanity = ghost.sanity.getState();
		pacManCollision = tile.equals(game.pacMan.tile());
	}

	public ActorRecord(Game game, Bonus bonus) {
		takesPart = bonus.visible;
		name = bonus.symbol != null ? bonus.toString() : "Bonus";
		tile = game.maze.bonusSeat.tile;
		if (bonus.getState() != null) {
			state = bonus.getState().name();
			ticksRemaining = bonus.state().getTicksRemaining();
			duration = bonus.state().getDuration();
		}
	}
}