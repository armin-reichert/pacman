package de.amr.games.pacman.view.theme.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

public interface IWorldRenderer extends IRenderer {

	void render(Graphics2D g, World world);

	default void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {

	}
}