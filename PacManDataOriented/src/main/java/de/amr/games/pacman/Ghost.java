package de.amr.games.pacman;

public class Ghost extends Creature {

	public final GhostCharacter character;
	public final V2i scatterTile;
	public V2i targetTile;
	public boolean frightened;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;
	public long bountyTimer;

	public Ghost(String name, GhostCharacter character, V2i homeTile, V2i scatterTile) {
		super(name, homeTile);
		this.character = character;
		this.scatterTile = scatterTile;
	}
}