package de.amr.games.pacman.actor;

import java.util.List;

import de.amr.games.pacman.model.Tile;

public interface MazeMover extends MazeResident {

	int moveDir();

	void setMoveDir(int dir);

	int nextDir();

	void setNextDir(int dir);

	boolean enteredNewTile();

	Tile targetTile();

	void setTargetTile(Tile tile);

	List<Tile> targetPath();

	void setTargetPath(List<Tile> path);

	boolean isStuck();

	boolean canCrossBorderTo(int dir);

	boolean canMoveBetween(Tile tile, Tile neighbor);

	Tile tilesAhead(int n);

}
