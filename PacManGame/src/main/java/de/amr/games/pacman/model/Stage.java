package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

import java.util.HashSet;
import java.util.Set;

import de.amr.games.pacman.controller.actor.MovingActor;

/**
 * Manages the actors which take part in the game.
 * 
 * @author Armin Reichert
 */
public class Stage {

	private Set<MovingActor<?>> actors = new HashSet<>();

	public boolean contains(MovingActor<?> actor) {
		return actors.contains(actor);
	}

	public void add(MovingActor<?> actor) {
		actors.add(actor);
		actor.init();
		actor.visible = true;
		loginfo("%s entered the stage", actor.name);
	}

	public void remove(MovingActor<?> actor) {
		actors.remove(actor);
		actor.visible = false;
		loginfo("%s left the stage", actor.name);
	}
}