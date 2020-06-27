package de.amr.games.pacman.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.games.pacman.model.map.PacManMaps;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.WorldGraph;
import de.amr.graph.grid.ui.rendering.ConfigurableGridRenderer;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.GridRenderer;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;

public class BoardPreview extends JFrame {

	static int TS = 16;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(BoardPreview::new);
	}

	private PacManWorld world;
	private WorldGraph graph;

	public BoardPreview() {
		world = new PacManWorld(PacManMaps.ARCADE_MAP);
		graph = new WorldGraph(world);
		setTitle("Pac-Man Maze Preview");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridCanvas canvas = new GridCanvas(graph, TS);
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
			if (world.isInaccessible(tile)) {
				return Color.LIGHT_GRAY;
			}
			if (world.isTunnel(tile)) {
				return Color.GRAY;
			}
			if (world.isDoor(tile)) {
				return Color.PINK;
			}
			if (world.insideGhostHouse(tile)) {
				return Color.CYAN;
			}
			if (world.isOneWayDown(tile)) {
				return Color.YELLOW;
			}
			if (world.isIntersection(tile)) {
				return Color.GREEN;
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
		if (tile.equals(world.ghostHouse().seat(0).tile) || tile.equals(world.horizonNE)) {
			return "B";
		}
		if (tile.equals(world.ghostHouse().seat(1).tile) || tile.equals(world.horizonSE)) {
			return "I";
		}
		if (tile.equals(world.ghostHouse().seat(2).tile) || tile.equals(world.horizonNW)) {
			return "P";
		}
		if (tile.equals(world.ghostHouse().seat(3).tile) || tile.equals(world.horizonSW)) {
			return "C";
		}
		if (tile.equals(world.bonusTile())) {
			return "$";
		}
		if (tile.equals(world.pacManSeat().tile)) {
			return "O";
		}
		if (world.isInaccessible(tile)) {
			return "";
		}
		if (world.containsSimplePellet(tile)) {
			return "o";
		}
		if (world.containsEnergizer(tile)) {
			return "Ö";
		}
		for (Portal portal : world.portals()) {
			if (portal.contains(tile)) {
				return "@";
			}
		}
		return "";
	}
}