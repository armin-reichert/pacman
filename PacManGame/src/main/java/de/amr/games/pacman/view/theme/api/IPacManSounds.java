package de.amr.games.pacman.view.theme.api;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public interface IPacManSounds {

	void loadMusicAsync();

	boolean isMusicLoadingComplete();

	void updateRunningClips();

	void playEatingPelletsSound();

	void stopEatingPelletsSound();

	void playClipEatBonus();

	void playClipEatGhost();

	void playClipExtraLife();

	void playMusicGameReady();

	void playClipGhostChasing();

	void loopClipGhostChasing();

	void stopClipGhostChasing();

	void playClipInsertCoin();

	void playClipPacManGainsPower();

	void playClipPacManLostPower();

	void playClipPacManDies();

	void stopAllClips();

	void stopAll();

	void playMusicGameRunning();
	
	void stopMusicGameRunning();

	void playMusicGameOver();

	boolean isGameOverMusicRunning();
}