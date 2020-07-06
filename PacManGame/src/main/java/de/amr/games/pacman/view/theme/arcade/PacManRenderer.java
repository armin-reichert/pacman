package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.IRenderer;

public class PacManRenderer extends SpriteRenderer implements IRenderer {

	private final World world;
	private final PacMan pacMan;

	public PacManRenderer(World world) {
		this.world = world;
		this.pacMan = world.population().pacMan();
		ArcadeThemeAssets assets = ArcadeTheme.ASSETS;
		Direction.dirs().forEach(dir -> sprites.set("walking-" + dir, assets.makeSprite_pacManWalking(dir)));
		sprites.set("collapsing", assets.makeSprite_pacManDying());
		sprites.set("full", assets.makeSprite_pacManFull());
		sprites.select("full");
	}

	@Override
	public void render(Graphics2D g) {
		if (world.isChangingLevel()) {
			selectSprite("full");
		} else {
			switch (pacMan.getState()) {
			case DEAD:
				selectSprite("full");
				if (pacMan.collapsing) {
					selectSprite("collapsing");
				} else {
					sprites.get("collapsing").resetAnimation();
				}
				break;
			case RUNNING:
				selectSprite("walking-" + pacMan.moveDir());
				enableAnimation(pacMan.tf.vx != 0 || pacMan.tf.vy != 0);
				break;
			case SLEEPING:
				selectSprite("full");
			default:
				break;
			}
		}
		drawEntity(g, pacMan);
	}
}