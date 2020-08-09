package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.IGhostRenderer;

/**
 * Renders a ghost using animated sprites.
 * 
 * @author Armin Reichert
 */
public class GhostRenderer extends SpriteRenderer implements IGhostRenderer {

	private static String keyColor(GhostPersonality personality, Direction dir) {
		return String.format("color-%s-%s", personality, dir);
	}

	private static String keyEyes(Direction dir) {
		return String.format("eyes-%s", dir);
	}

	private static String keyPoints(int points) {
		return String.format("points-%d", points);
	}

	public GhostRenderer() {
		ArcadeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		for (Direction dir : Direction.values()) {
			for (GhostPersonality personality : GhostPersonality.values()) {
				spriteMap.set(keyColor(personality, dir), arcadeSprites.makeSprite_ghostColored(personality, dir));
			}
			spriteMap.set(keyEyes(dir), arcadeSprites.makeSprite_ghostEyes(dir));
		}
		spriteMap.set("frightened", arcadeSprites.makeSprite_ghostFrightened());
		spriteMap.set("flashing", arcadeSprites.makeSprite_ghostFlashing());
		for (int bounty : Game.GHOST_BOUNTIES) {
			spriteMap.set(keyPoints(bounty), arcadeSprites.makeSprite_number(bounty));
		}
	}

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		selectSprite(ghost);
		spriteMap.current().get().enableAnimation(ghost.isEnabled());
		drawEntitySprite(g, ghost.entity, 2);
	}

	private void selectSprite(Ghost ghost) {
		GhostState state = ghost.getState();
		GhostPersonality personality = ghost.getPersonality();
		Direction dir = ghost.moveDir();
		if (state == null) {
			spriteMap.select(keyColor(personality, dir));
		} else {
			switch (state) {
			case LOCKED:
			case LEAVING_HOUSE:
			case CHASING:
			case SCATTERING:
				spriteMap.select(keyColor(personality, dir));
				break;
			case ENTERING_HOUSE:
				spriteMap.select(keyEyes(dir));
				break;
			case FRIGHTENED:
				spriteMap.select(ghost.isFlashing() ? "flashing" : "frightened");
				break;
			case DEAD:
				spriteMap.select(ghost.getBounty() == 0 ? keyEyes(dir) : keyPoints(ghost.getBounty()));
				break;
			default:
				break;
			}
		}
	}
}