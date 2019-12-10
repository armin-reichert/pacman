package de.amr.games.pacman.actor;

import java.util.List;

import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that can move through a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeMover extends MazeResident {

	byte moveDir();

	void setMoveDir(byte dir);

	byte nextDir();

	void setNextDir(byte dir);

	boolean enteredNewTile();

	Tile targetTile();

	void setTargetTile(Tile tile);

	List<Tile> targetPath();

	void setTargetPath(List<Tile> path);

	boolean canMoveForward();

	boolean canCrossBorderTo(byte dir);

	boolean canMoveBetween(Tile tile, Tile neighbor);

	Tile tilesAhead(int n);
}