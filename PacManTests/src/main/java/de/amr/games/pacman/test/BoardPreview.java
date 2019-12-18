package de.amr.games.pacman.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.MazeGraph;
import de.amr.games.pacman.model.PacManGame;
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
	private MazeGraph graph;

	public BoardPreview() {
		maze = new Maze(PacManGame.BOARD);
		graph = new MazeGraph(maze);
		setTitle("Pac-Man Maze Preview");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridCanvas canvas = new GridCanvas(graph.grid, TS);
		canvas.pushRenderer(createRenderer());
		add(canvas, BorderLayout.CENTER);
		canvas.drawGrid();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private Tile tile(int cell) {
		return maze.tileAt(graph.grid.col(cell), graph.grid.row(cell));
	}

	private GridRenderer createRenderer() {
		ConfigurableGridRenderer r = new WallPassageGridRenderer();
		r.fnCellSize = () -> TS;
		r.fnPassageWidth = (u, v) -> TS - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> {
			Tile tile = tile(cell);
			if (tile.isWall()) {
				return Color.BLACK;
			}
			if (tile.isTunnel()) {
				return Color.LIGHT_GRAY;
			}
			if (maze.inGhostHouse(tile)) {
				return Color.CYAN;
			}
			if (maze.isNoUpIntersection(tile)) {
				return Color.YELLOW;
			}
			if (maze.isIntersection(tile)) {
				return Color.GREEN;
			}
			if (tile.isDoor()) {
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
		Tile tile = tile(cell);
		if (tile == maze.ghostHome[0] || tile == maze.horizonNE) {
			return "B";
		}
		if (tile == maze.ghostHome[1] || tile == maze.horizonSE) {
			return "I";
		}
		if (tile == maze.ghostHome[2] || tile == maze.horizonNW) {
			return "P";
		}
		if (tile == maze.ghostHome[3] || tile == maze.horizonSW) {
			return "C";
		}
		if (tile == maze.bonusTile) {
			return "$";
		}
		if (tile == maze.pacManHome) {
			return "O";
		}
		if (tile.isWall()) {
			return "";
		}
		if (tile.containsEnergizer()) {
			return "E";
		}
		return String.valueOf(tile.content);
	}
}