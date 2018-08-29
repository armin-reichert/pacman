package de.amr.games.pacman.view.core;

import de.amr.easy.game.view.Controller;

public interface AnimationController extends Controller {

	void start();

	void stop();

	boolean isCompleted();
}