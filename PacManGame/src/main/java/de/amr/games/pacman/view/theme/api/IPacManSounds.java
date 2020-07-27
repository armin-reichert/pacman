package de.amr.games.pacman.view.theme.api;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public interface IPacManSounds {

	void loadMusicAsync();

	boolean isMusicLoadingComplete();

	void playEatingPelletsSound();

	void stopEatingPelletsSound();
	
	boolean isEeatingPelletsSoundRunning();

	void playClipEatBonus();

	void playClipEatGhost();

	void playClipExtraLife();

	void playMusicGameReady();

	void playClipGhostChasing();

	void loopClipGhostChasing();

	void stopClipGhostChasing();
	
	boolean isClipGhostChasingRunning();
	
	void playClipGhostDead();

	void loopClipGhostDead();

	void stopClipGhostDead();
	
	boolean isClipGhostDeadRunning();

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