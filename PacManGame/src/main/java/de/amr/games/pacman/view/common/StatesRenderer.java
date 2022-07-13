/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.view.common;

import static de.amr.games.pacman.PacManApp.appSettings;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.view.common.Rendering.ghostColor;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;

public class StatesRenderer {

	private static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 6);

	public void renderStates(Graphics2D g, Folks folks, GhostCommand ghostCommand) {
		drawActorStates(g, folks, ghostCommand);
		drawActorsOffTrack(g, folks);
	}

	private void drawActorStates(Graphics2D g, Folks folks, GhostCommand ghostCommand) {
		folks.ghostsInWorld().forEach(ghost -> drawGhostState(g, ghost, ghostCommand));
		drawPacManState(g, folks.pacMan);
	}

	private void drawPacManState(Graphics2D g, PacMan pacMan) {
		if (!pacMan.visible || pacMan.ai.getState() == null) {
			return;
		}
		String text = pacMan.ai.getState().name();
		if (pacMan.ai.is(POWERFUL)) {
			text += String.format("(%d)", pacMan.ai.state(POWERFUL).getTicksRemaining());
		}
		if (pacMan.ai.state().hasTimer()) {
			text += String.format("(%d of %d)", pacMan.ai.state().getTicksConsumed(), pacMan.ai.state().getDuration());
		}
		if (appSettings.pacManImmortable) {
			text += " lives " + Rendering.INFTY;
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
		if (ghost.ai.getState() == null) {
			return; // may happen in test applications where not all ghosts are used
		}
		StringBuilder text = new StringBuilder();
		// show ghost name if not obvious
		text.append(ghost.ai.is(DEAD, FRIGHTENED, ENTERING_HOUSE) ? ghost.name : "");
		// chasing or scattering time
		if (ghostCommand != null && ghost.ai.is(SCATTERING, CHASING)) {
			long remaining = ghostCommand.state().getTicksRemaining();
			String remainingText = formatLargeTicks(remaining);
			long duration = ghostCommand.state().getDuration();
			String durationText = formatLargeTicks(duration);
			text.append(String.format("(%s,%s|%s)", ghost.ai.getState(), remainingText, durationText));
		} else {
			if (ghost.ai.state().hasTimer()) {
				text.append(String.format("(%s,%d|%d)", ghost.ai.getState(), ghost.ai.state().getTicksRemaining(),
						ghost.ai.state().getDuration()));
			} else {
				text.append(ghost.ai.getState());
			}
		}
		drawEntityState(g, ghost, text.toString(), ghostColor(ghost));
	}

	private String formatLargeTicks(long ticks) {
		if (ticks <= 1000) {
			return String.valueOf(ticks);
		}
		if (ticks <= 10_000) {
			return ">1000";
		}
		if (ticks <= 100_000) {
			return ">10_000";
		}
		return ">100_000";
	}

	private void drawActorsOffTrack(Graphics2D g, Folks folks) {
		drawActorOffTrack(g, folks.pacMan);
		folks.ghostsInWorld().forEach(ghost -> drawActorOffTrack(g, ghost));
	}

	private void drawActorOffTrack(Graphics2D g, Guy guy) {
		if (!guy.visible) {
			return;
		}
		Stroke normal = g.getStroke();
		Stroke fine = new BasicStroke(0.2f);
		g.setStroke(fine);
		g.setColor(Color.RED);
		g.translate(guy.tf.x, guy.tf.y);
		int w = guy.tf.width, h = guy.tf.height;
		Direction moveDir = guy.moveDir;
		if ((moveDir == Direction.LEFT || moveDir == Direction.RIGHT) && round(guy.tf.y) % Tile.SIZE != 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if ((moveDir == Direction.UP || moveDir == Direction.DOWN) && round(guy.tf.x) % Tile.SIZE != 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-guy.tf.x, -guy.tf.y);
		g.setStroke(normal);
	}
}