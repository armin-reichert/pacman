package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;

public class MazeRoute {

	public int dir;

	public List<Tile> path = Collections.emptyList();
	
	public Tile targetTile; 
}