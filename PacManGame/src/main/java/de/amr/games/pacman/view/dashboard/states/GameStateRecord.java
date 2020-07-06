package de.amr.games.pacman.view.dashboard.states;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.core.Tile;

/**
 * Holds the actor data displayed in the game state view.
 * 
 * @author Armin Reichert
 */
class GameStateRecord {
	public Creature<?> creature;
	public boolean included;
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
}