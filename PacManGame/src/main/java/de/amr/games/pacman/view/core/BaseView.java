package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Base class of all views in the game.
 * 
 * @author Armin Reichert
 */
public abstract class BaseView implements Lifecycle, View {

	public final World world;
	public final Theme theme;
	public final int width;
	public final int height;

	public BaseView(World world, Theme theme) {
		this(world, theme, app().settings().width, app().settings().height);
	}

	public BaseView(World world, Theme theme, int width, int height) {
		this.world = world;
		this.theme = theme;
		this.width = width;
		this.height = height;
	}
}