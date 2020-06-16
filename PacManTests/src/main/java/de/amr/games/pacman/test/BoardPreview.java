package de.amr.games.pacman.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.MazeGraph;
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
		maze = new Maze();
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

	private GridRenderer createRenderer() {
		ConfigurableGridRenderer r = new WallPassageGridRenderer();
		r.fnCellSize = () -> TS;
		r.fnPassageWidth = (u, v) -> TS - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> {
			Tile tile = graph.tile(cell);
			if (maze.isWall(tile)) {
				return Color.LIGHT_GRAY;
			}
			if (maze.isTunnel(tile)) {
				return Color.GRAY;
			}
			if (maze.insideGhostHouse(tile)) {
				return Color.CYAN;
			}
			if (maze.isOneWayDown(tile)) {
				return Color.YELLOW;
			}
			if (maze.isIntersection(tile)) {
				return Color.GREEN;
			}
			if (maze.isDoor(tile)) {
				return Color.PINK;
			}
			return Color.WHITE;
		};
		r.fnText = this::text;
		r.fnTextColor = cell -> Color.BLUE;
		r.fnTextFont = cell -> new Font("Arial Bold", Font.BOLD, TS * 75 / 100);
		return r;
	}

	private String text(int cell) {
		Tile tile = graph.tile(cell);
		if (tile.equals(maze.ghostSeats[0].tile) || tile.equals(maze.horizonNE)) {
			return "B";
		}
		if (tile.equals(maze.ghostSeats[1].tile) || tile.equals(maze.horizonSE)) {
			return "I";
		}
		if (tile.equals(maze.ghostSeats[2].tile) || tile.equals(maze.horizonNW)) {
			return "P";
		}
		if (tile.equals(maze.ghostSeats[3].tile) || tile.equals(maze.horizonSW)) {
			return "C";
		}
		if (tile.equals(maze.bonusSeat.tile)) {
			return "$";
		}
		if (tile.equals(maze.pacManSeat.tile)) {
			return "O";
		}
		if (maze.isWall(tile)) {
			return "";
		}
		if (maze.containsSimplePellet(tile)) {
			return "o";
		}
		if (maze.containsEnergizer(tile)) {
			return "Ö";
		}
		return " ";
	}
}