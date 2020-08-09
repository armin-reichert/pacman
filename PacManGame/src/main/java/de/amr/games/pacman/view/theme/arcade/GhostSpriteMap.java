package de.amr.games.pacman.view.theme.arcade;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;

class GhostSpriteMap extends SpriteMap {

	public String keyColor(GhostPersonality personality, Direction dir) {
		return String.format("colored-%s-%s", personality, dir);
	}

	public String keyEyes(Direction dir) {
		return String.format("eyes-%s", dir);
	}

	public String keyPoints(int points) {
		return String.format("points-%d", points);
	}

	public GhostSpriteMap(Ghost ghost) {
		ArcadeSprites sprites = ArcadeTheme.THEME.$value("sprites");
		for (Direction dir : Direction.values()) {
			for (GhostPersonality personality : GhostPersonality.values()) {
				set(keyColor(personality, dir), sprites.makeSprite_ghostColored(personality, dir));
			}
			set(keyEyes(dir), sprites.makeSprite_ghostEyes(dir));
		}
		set("frightened", sprites.makeSprite_ghostFrightened());
		set("flashing", sprites.makeSprite_ghostFlashing());
		for (int bounty : Game.GHOST_BOUNTIES) {
			set(keyPoints(bounty), sprites.makeSprite_number(bounty));
		}
	}
}