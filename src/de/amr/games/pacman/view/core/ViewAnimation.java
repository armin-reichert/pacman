package de.amr.games.pacman.view.core;

import de.amr.easy.game.view.ViewController;

public interface ViewAnimation extends ViewController {

	void start();

	void stop();

	boolean isCompleted();
}