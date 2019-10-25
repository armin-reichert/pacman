package de.amr.games.pacman.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.ui.rendering.ConfigurableGridRenderer;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.GridRenderer;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;

public class BoardPreview extends JFrame {

	static int TS = 16;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(BoardPreview::new);
	}

	private Maze maze;

	public BoardPreview() {
		maze = new Maze();
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
		r.fnPassageWidth = (u, v) -> TS - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> {
			Tile tile = maze.tile(cell);
			if (maze.isWall(tile)) {
				return Color.BLACK;
			}
			if (maze.inTunnel(tile)) {
				return Color.LIGHT_GRAY;
			}
			if (maze.inGhostHouse(tile)) {
				return Color.CYAN;
			}
			if (maze.isUnrestrictedIntersection(tile)) {
				return Color.GREEN;
			}
			if (maze.isUpwardsBlockedIntersection(tile)) {
				return Color.YELLOW;
			}
			if (maze.isDoor(tile)) {
				return Color.PINK;
			}
			return Color.WHITE;
		};
		r.fnText = this::text;
		r.fnTextColor = cell -> Color.RED;
		r.fnTextFont = cell -> new Font("Arial Bold", Font.BOLD, TS * 70 / 100);
		return r;
	}

	private String text(int cell) {
		Tile tile = maze.tile(cell);
		if (tile == maze.getBlinkyHome() || tile == maze.getBlinkyScatterTarget()) {
			return "B";
		}
		if (tile == maze.getPinkyHome() || tile == maze.getPinkyScatterTarget()) {
			return "P";
		}
		if (tile == maze.getInkyHome() || tile == maze.getInkyScatterTarget()) {
			return "I";
		}
		if (tile == maze.getClydeHome() || tile == maze.getClydeScatterTarget()) {
			return "C";
		}
		if (maze.isWall(tile)) {
			return "";
		}
		if (maze.containsEnergizer(tile)) {
			return "E";
		}
		return String.valueOf(maze.getGraph().get(cell));
	}
}