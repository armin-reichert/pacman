package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;

public class MazeRoute {

	private int dir;
	private List<Tile> path = Collections.emptyList();
	private Tile targetTile;

	public MazeRoute() {
		this(-1);
	}

	public MazeRoute(int dir) {
		this.dir = dir;
	}

	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public List<Tile> getPath() {
		return Collections.unmodifiableList(path);
	}

	public void setPath(List<Tile> path) {
		this.path = path;
	}

	public Tile getTargetTile() {
		return targetTile;
	}

	public void setTargetTile(Tile targetTile) {
		this.targetTile = targetTile;
	}
}