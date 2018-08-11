package de.amr.games.pacman.test;

import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.ENERGIZER;
import static de.amr.games.pacman.model.Content.PELLET;
import static de.amr.games.pacman.model.Content.TUNNEL;
import static de.amr.games.pacman.model.Content.WALL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.ui.swing.rendering.ConfigurableGridRenderer;
import de.amr.easy.grid.ui.swing.rendering.GridCanvas;
import de.amr.easy.grid.ui.swing.rendering.GridRenderer;
import de.amr.easy.grid.ui.swing.rendering.WallPassageGridRenderer;
import de.amr.games.pacman.model.Maze;

public class BoardPreview extends JFrame {

	static int TS = 32;

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
		Map<Character, Color> colors = new HashMap<>();
		colors.put(WALL, Color.BLUE);
		colors.put(PELLET, Color.WHITE);
		colors.put(ENERGIZER, Color.GREEN);
		colors.put(DOOR, Color.ORANGE);
		colors.put(TUNNEL, Color.GRAY);
		ConfigurableGridRenderer r = new WallPassageGridRenderer();
		r.fnCellSize = () -> TS;
		r.fnPassageWidth = () -> TS - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> colors.getOrDefault(maze.getGraph().get(cell), Color.WHITE);
		r.fnText = cell -> String.valueOf(maze.getGraph().get(cell));
		r.fnTextFont = () -> new Font("Arial Bold", Font.BOLD, TS / 2);
		return r;
	}
}