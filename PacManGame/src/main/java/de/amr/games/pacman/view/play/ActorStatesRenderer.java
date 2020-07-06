package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.view.theme.common.Rendering.ghostColor;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

public class ActorStatesRenderer implements IRenderer {

	private static final Font SMALL_FONT = new Font("Arial Narrow", Font.PLAIN, 6);

	private final World world;
	private GhostCommand ghostCommand;

	public ActorStatesRenderer(World world, GhostCommand ghostCommand) {
		this.world = world;
	}

	@Override
	public void render(Graphics2D g) {
		drawActorStates(g);
		drawActorsOffTrack(g);
	}

	private void drawActorStates(Graphics2D g) {
		world.population().ghosts().filter(world::included).forEach(ghost -> drawGhostState(g, ghost, ghostCommand));
		drawPacManState(g, world.population().pacMan());
	}

	private void drawPacManState(Graphics2D g, PacMan pacMan) {
		if (!pacMan.visible || pacMan.getState() == null) {
			return;
		}
		String text = pacMan.power > 0 ? String.format("POWER(%d)", pacMan.power) : pacMan.getState().name();
		if (settings.pacManImmortable) {
			text += " immortable";
		}
		drawEntityState(g, pacMan, text, Color.YELLOW);
	}

	private void drawEntityState(Graphics2D g, Entity entity, String text, Color color) {
		try (Pen pen = new Pen(g)) {
			pen.color(color);
			pen.font(SMALL_FONT);
			pen.drawCentered(text, entity.tf.getCenter().x, entity.tf.getCenter().y - 2);
		}
	}

	private void drawGhostState(Graphics2D g, Ghost ghost, GhostCommand ghostCommand) {
		if (!ghost.visible) {
			return;
		}
		if (ghost.getState() == null) {
			return; // may happen in test applications where not all ghosts are used
		}
		StringBuilder text = new StringBuilder();
		// show ghost name if not obvious
		text.append(ghost.is(DEAD, FRIGHTENED, ENTERING_HOUSE) ? ghost.name : "");
		// timer values
		int duration = ghost.state().getDuration();
		int remaining = ghost.state().getTicksRemaining();
		// chasing or scattering time
		if (ghostCommand != null && ghost.is(SCATTERING, CHASING)) {
			if (ghostCommand.state() != null) {
				duration = ghostCommand.state().getDuration();
				remaining = ghostCommand.state().getTicksRemaining();
			}
		}
		if (duration != Integer.MAX_VALUE) {
			text.append(String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		} else {
			text.append(String.format("(%s,%s)", ghost.getState(), Rendering.INFTY));
		}
		if (ghost.is(LEAVING_HOUSE)) {
			text.append(String.format("[->%s]", ghost.subsequentState));
		}
		drawEntityState(g, ghost, text.toString(), ghostColor(ghost));
	}

	private void drawActorsOffTrack(Graphics2D g) {
		world.population().creatures().filter(world::included).forEach(actor -> drawActorOffTrack(actor, g));
	}

	private void drawActorOffTrack(Creature<?> actor, Graphics2D g) {
		if (!actor.visible) {
			return;
		}
		Stroke normal = g.getStroke();
		Stroke fine = new BasicStroke(0.2f);
		g.setStroke(fine);
		g.setColor(Color.RED);
		g.translate(actor.tf.x, actor.tf.y);
		int w = actor.tf.width, h = actor.tf.height;
		Direction moveDir = actor.moveDir();
		if ((moveDir == Direction.LEFT || moveDir == Direction.RIGHT) && round(actor.tf.y) % Tile.SIZE != 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if ((moveDir == Direction.UP || moveDir == Direction.DOWN) && round(actor.tf.x) % Tile.SIZE != 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf.x, -actor.tf.y);
		g.setStroke(normal);
	}
}