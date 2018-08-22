package de.amr.games.pacman.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.ui.swing.rendering.ConfigurableGridRenderer;
import de.amr.easy.grid.ui.swing.rendering.GridCanvas;
import de.amr.easy.grid.ui.swing.rendering.GridRenderer;
import de.amr.easy.grid.ui.swing.rendering.WallPassageGridRenderer;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class BoardPreview extends JFrame {

	static int TS = 16;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(BoardPreview::new);
	}

	private Maze maze;

	public BoardPreview() {
		maze = new Maze(Assets.text("maze.txt"));
		setTitle("Pac-Man Maze Preview");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridCanvas canvas = new GridCanvas(maze.getGraph(), TS);
		canvas.pushRenderer(createRenderer());
		add(canvas, BorderLayout.CENTER);
		canvas.drawGrid();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private GridRenderer createRenderer() {
		ConfigurableGridRenderer r = new WallPassageGridRenderer();
		r.fnCellSize = () -> TS;
		r.fnPassageWidth = () -> TS - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> {
			Tile tile = maze.tile(cell);
			if (maze.isWall(tile)) {
				return Color.BLUE;
			}
			if (maze.inTunnel(tile)) {
				return Color.LIGHT_GRAY;
			}
			if (maze.inGhostHouse(tile)) {
				return Color.CYAN;
			}
			if (maze.isIntersection(tile)) {
				return Color.GREEN;
			}
			if (maze.isRestrictedIntersection(tile)) {
				return Color.YELLOW;
			}
			if (maze.isDoor(tile)) {
				return Color.PINK;
			}
			return Color.WHITE;
		};
		r.fnText = cell -> String.valueOf(maze.getGraph().get(cell));
		r.fnTextFont = () -> new Font("Arial Bold", Font.BOLD, TS / 2);
		return r;
	}
}