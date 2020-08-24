package de.amr.games.pacman.view.theme.arcade;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.api.Direction;

public class PacManSpriteMap extends SpriteMap {

	public PacManSpriteMap() {
		ArcadeSprites sprites = ArcadeTheme.THEME.$value("sprites");
		Direction.dirs().forEach(dir -> set("walking-" + dir, sprites.makeSprite_pacManWalking(dir)));
		set("collapsing", sprites.makeSprite_pacManCollapsing());
		set("full", sprites.makeSprite_pacManFull());
	}
}
