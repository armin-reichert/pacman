package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.sec;

import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Holds the actor data displayed in the game state view.
 * 
 * @author Armin Reichert
 */
class ActorData {
	boolean onStage;
	String name;
	Tile tile;
	Tile target;
	Direction moveDir;
	Direction wishDir;
	float speed;
	String state;
	int ticksRemaining;
	int duration;
	boolean pacManCollision;

	public ActorData(Game game, PacMan pacMan) {
		onStage = game.onStage(pacMan);
		name = "Pac-Man";
		tile = pacMan.tile();
		moveDir = pacMan.moveDir();
		wishDir = pacMan.wishDir();
		speed = pacMan.currentSpeed(game);
		state = pacMan.power == 0 ? pacMan.getState().name() : "POWER";
		ticksRemaining = pacMan.power == 0 ? pacMan.state().getTicksRemaining() : pacMan.power;
		duration = pacMan.power == 0 ? pacMan.state().getDuration() : sec(game.level.pacManPowerSeconds);
	}

	public ActorData(Game game, GhostCommand ghostCommand, Ghost ghost) {
		onStage = game.onStage(ghost);
		name = ghost.name;
		tile = ghost.tile();
		target = ghost.targetTile();
		moveDir = ghost.moveDir();
		wishDir = ghost.wishDir();
		speed = ghost.currentSpeed(game);
		state = ghost.getState().name();
		ticksRemaining = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getTicksRemaining()
				: ghost.state().getTicksRemaining();
		duration = ghost.is(CHASING, SCATTERING) ? ghostCommand.state().getDuration() : ghost.state().getDuration();
		pacManCollision = tile.equals(game.pacMan.tile());
	}

	public ActorData(Game game, Bonus bonus) {
		onStage = bonus.visible;
		name = bonus.symbol != null ? bonus.toString() : "Bonus";
		tile = game.maze.bonusSeat.tile;
		state = bonus.getState().name();
		ticksRemaining = bonus.state().getTicksRemaining();
		duration = bonus.state().getDuration();
	}
}