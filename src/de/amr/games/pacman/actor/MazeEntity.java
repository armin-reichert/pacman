package de.amr.games.pacman.actor;

import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.PacManGameUI;

public abstract class MazeEntity extends GameEntity {

	public int row() {
		return round(tf.getY() + getHeight() / 2) / PacManGameUI.TS;
	}

	public int col() {
		return round(tf.getX() + getWidth() / 2) / PacManGameUI.TS;
	}

	public Tile getTile() {
		return new Tile(col(), row());
	}

	public void placeAt(Tile tile) {
		placeAt(tile.col, tile.row);
	}

	public void placeAt(int col, int row) {
		tf.moveTo(col * PacManGameUI.TS, row * PacManGameUI.TS);
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % PacManGameUI.TS == 0 && round(tf.getY()) % PacManGameUI.TS == 0;
	}

	@Override
	public void draw(Graphics2D g) {
		// by default, draw sprite centered over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2;
		int dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public int getWidth() {
		return PacManGameUI.TS;
	}

	@Override
	public int getHeight() {
		return PacManGameUI.TS;
	}
}