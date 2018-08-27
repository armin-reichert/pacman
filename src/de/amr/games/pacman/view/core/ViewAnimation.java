package de.amr.games.pacman.view.core;


public interface ViewAnimation {
	
	default void initAnimation() {
		
	}
	
	default void startAnimation() {
		
	}
	
	default void stopAnimation() {
		
	}
	
	default void updateAnimation() {
		
	}
	
	default boolean isAnimationCompleted() {
		return false;
	}
}
